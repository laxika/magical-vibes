package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TypeLineParserTest {
    @Test
    void parsesPlanarTypesAndKeepsMultiwordPlanarSubtypeTogether() {
        var plane = TypeLineParser.parse("Plane \u2014 Serra's Realm");
        assertThat(plane.type()).isEqualTo(CardType.PLANE);
        assertThat(plane.subtypes()).containsExactly(CardSubtype.SERRAS_REALM);
        assertThat(plane.type().isPermanentType()).isFalse();
        var phenomenon = TypeLineParser.parse("Phenomenon");
        assertThat(phenomenon.type()).isEqualTo(CardType.PHENOMENON);
        assertThat(phenomenon.type().isPermanentType()).isFalse();
    }

    @Test
    void stillParsesOrdinaryMultitypeCardsAndMultipleCreatureSubtypes() {
        var creature = TypeLineParser.parse("Artifact Creature \u2014 Human Wizard");
        assertThat(creature.type()).isEqualTo(CardType.ARTIFACT);
        assertThat(creature.additionalTypes()).containsExactly(CardType.CREATURE);
        assertThat(creature.subtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.WIZARD);
    }
}
