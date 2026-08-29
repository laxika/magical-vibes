package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GravitationalShiftTest extends BaseCardTest {

    private static Card creature(boolean flying) {
        Card card = new Card();
        card.setName(flying ? "Flying Creature" : "Ground Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(flying ? Set.of(Keyword.FLYING) : Set.of());
        return card;
    }

    @Test
    @DisplayName("Boosts creatures with flying and shrinks creatures without flying")
    void modifiesCreaturesBasedOnFlying() {
        harness.addToBattlefield(player1, new GravitationalShift());
        Permanent ownFlyer = harness.addToBattlefieldAndReturn(player1, creature(true));
        Permanent ownGroundCreature = harness.addToBattlefieldAndReturn(player1, creature(false));
        Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, creature(true));
        Permanent opposingGroundCreature = harness.addToBattlefieldAndReturn(player2, creature(false));

        assertThat(gqs.getEffectivePower(gd, ownFlyer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownFlyer)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownGroundCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, ownGroundCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingFlyer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingFlyer)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingGroundCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, opposingGroundCreature)).isEqualTo(2);
    }
}
