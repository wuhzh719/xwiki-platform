/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.model.internal.reference;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.EntityReferenceValueProvider;

/**
 * Unit tests for {@link DefaultStringEntityReferenceSerializer}.
 * 
 * @version $Id$
 * @since 2.2M1
 */
public class DefaultStringEntityReferenceSerializerTest
{
    private static final String DEFAULT_WIKI = "defwiki";

    private static final String DEFAULT_SPACE = "defspace";

    private static final String DEFAULT_PAGE = "defpage";

    private static final String DEFAULT_ATTACHMENT = "deffilename";

    private EntityReferenceSerializer serializer;

    private EntityReferenceResolver resolver;

    private Mockery mockery = new Mockery();

    @Before
    public void setUp()
    {
        this.serializer = new DefaultStringEntityReferenceSerializer();
        
        this.resolver = new DefaultStringEntityReferenceResolver();
        final EntityReferenceValueProvider mockValueProvider = this.mockery.mock(EntityReferenceValueProvider.class);
        ReflectionUtils.setFieldValue(this.resolver, "provider", mockValueProvider);

        this.mockery.checking(new Expectations() {{
            allowing(mockValueProvider).getDefaultValue(EntityType.WIKI);
                will(returnValue(DEFAULT_WIKI));
            allowing(mockValueProvider).getDefaultValue(EntityType.SPACE);
                will(returnValue(DEFAULT_SPACE));
            allowing(mockValueProvider).getDefaultValue(EntityType.DOCUMENT);
                will(returnValue(DEFAULT_PAGE));
            allowing(mockValueProvider).getDefaultValue(EntityType.ATTACHMENT);
                will(returnValue(DEFAULT_ATTACHMENT));
        }});
    }

    @Test
    public void testSerializeDocumentReference() throws Exception
    {
        EntityReference reference = resolver.resolve("wiki:space.page", EntityType.DOCUMENT);
        Assert.assertEquals("wiki:space.page", serializer.serialize(reference));

        reference = resolver.resolve("wiki:space.", EntityType.DOCUMENT);
        Assert.assertEquals("wiki:space.defpage", serializer.serialize(reference));

        reference = resolver.resolve("space.", EntityType.DOCUMENT);
        Assert.assertEquals("defwiki:space.defpage", serializer.serialize(reference));

        reference = resolver.resolve("page", EntityType.DOCUMENT);
        Assert.assertEquals("defwiki:defspace.page", serializer.serialize(reference));

        reference = resolver.resolve(".", EntityType.DOCUMENT);
        Assert.assertEquals("defwiki:defspace.defpage", serializer.serialize(reference));

        reference = resolver.resolve(null, EntityType.DOCUMENT);
        Assert.assertEquals("defwiki:defspace.defpage", serializer.serialize(reference));

        reference = resolver.resolve("", EntityType.DOCUMENT);
        Assert.assertEquals("defwiki:defspace.defpage", serializer.serialize(reference));

        reference = resolver.resolve("wiki1.wiki2:wiki3:some.space.page", EntityType.DOCUMENT);
        Assert.assertEquals("wiki1.wiki2:wiki3:some\\.space.page", serializer.serialize(reference));

        reference = resolver.resolve("some.space.page", EntityType.DOCUMENT);
        Assert.assertEquals("defwiki:some\\.space.page", serializer.serialize(reference));

        reference = resolver.resolve("wiki:page", EntityType.DOCUMENT);
        Assert.assertEquals("defwiki:defspace.wiki:page", serializer.serialize(reference));

        // Verify that passing null doesn't throw a NPE
        Assert.assertNull(serializer.serialize(null));
        
        // Test escapes

        reference = resolver.resolve("\\.:@\\.", EntityType.DOCUMENT);
        Assert.assertEquals("defwiki:defspace.\\.:@\\.", serializer.serialize(reference));
    }

    @Test
    public void testSerializeSpaceReference() throws Exception
    {
        EntityReference reference = resolver.resolve("wiki:space1.space2", EntityType.SPACE);
        Assert.assertEquals("wiki:space1\\.space2", serializer.serialize(reference));
    }

    @Test
    public void testSerializeAttachmentReference() throws Exception
    {
        EntityReference reference = resolver.resolve("wiki:space.page@filename", EntityType.ATTACHMENT);
        Assert.assertEquals("wiki:space.page@filename", serializer.serialize(reference));

        reference = resolver.resolve("", EntityType.ATTACHMENT);
        Assert.assertEquals("defwiki:defspace.defpage@deffilename", serializer.serialize(reference));

        reference = resolver.resolve("wiki:space.page@my.png", EntityType.ATTACHMENT);
        Assert.assertEquals("wiki:space.page@my.png", serializer.serialize(reference));

        reference = resolver.resolve("some:file.name", EntityType.ATTACHMENT);
        Assert.assertEquals("defwiki:defspace.defpage@some:file.name", serializer.serialize(reference));

        // Test escapes

        reference = resolver.resolve(":.\\@", EntityType.ATTACHMENT);
        Assert.assertEquals("defwiki:defspace.defpage@:.\\@", serializer.serialize(reference));
    }
    
    @Test
    public void testSerializeReferenceWithChild() 
    {
        EntityReference reference = resolver.resolve("wiki:Space.Page", EntityType.DOCUMENT);
        Assert.assertEquals("wiki:Space", serializer.serialize(reference.getParent()));
        
        Assert.assertEquals("wiki", serializer.serialize(reference.getParent().getParent()));
    }
}