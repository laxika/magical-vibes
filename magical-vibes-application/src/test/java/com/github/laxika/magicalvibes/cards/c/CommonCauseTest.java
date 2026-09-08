package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommonCauseTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor... colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        if (colors.length > 0) {
            card.setColor(colors[0]);
            card.setColors(List.of(colors));
        }
        return card;
    }

    private static Card createArtifactCreature(String name, int power, int toughness,
                                                CardColor... colors) {
        Card card = createCreature(name, power, toughness, colors);
        card.setType(CardType.ARTIFACT);
        card.setAdditionalTypes(EnumSet.of(CardType.CREATURE));
        return card;
    }

    private Permanent addCommonCause() {
        harness.addToBattlefield(player1, new CommonCause());
        return findPermanent(player1, "Common Cause");
    }

    @Test
    @DisplayName("Boosts nonartifact creatures when they all share a color")
    void boostsWhenAllNonartifactCreaturesShareColor() {
        addCommonCause();
        harness.addToBattlefield(player1, createCreature("White Creature", 2, 2, CardColor.WHITE));
        harness.addToBattlefield(player2, createCreature("White Blue Creature", 3, 3,
                CardColor.WHITE, CardColor.BLUE));

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "White Creature"))).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "White Blue Creature"))).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not boost when nonartifact creatures have no shared color")
    void doesNotBoostWhenColorsDoNotOverlap() {
        addCommonCause();
        harness.addToBattlefield(player1, createCreature("White Creature", 2, 2, CardColor.WHITE));
        harness.addToBattlefield(player2, createCreature("Blue Creature", 3, 3, CardColor.BLUE));

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "White Creature"))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Blue Creature"))).isEqualTo(3);
    }

    @Test
    @DisplayName("Ignores artifact creatures when checking and applying the boost")
    void ignoresArtifactCreatures() {
        addCommonCause();
        harness.addToBattlefield(player1, createCreature("White Creature", 2, 2, CardColor.WHITE));
        harness.addToBattlefield(player2, createArtifactCreature("Colorless Artifact Creature", 3, 3));

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "White Creature"))).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd,
                findPermanent(player2, "Colorless Artifact Creature"))).isEqualTo(3);
    }

    @Test
    @DisplayName("A colorless nonartifact creature prevents the boost")
    void colorlessNonartifactCreaturePreventsBoost() {
        addCommonCause();
        harness.addToBattlefield(player1, createCreature("White Creature", 2, 2, CardColor.WHITE));
        harness.addToBattlefield(player2, createCreature("Colorless Creature", 3, 3));

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "White Creature"))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Colorless Creature"))).isEqualTo(3);
    }
}
