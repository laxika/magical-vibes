package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KyoshiWarriors.class)
class KyoshiWarriorsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 white Ally token")
    void etbCreatesAllyToken() {
        harness.setHand(player1, List.of(new KyoshiWarriors()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent ally = findPermanent(player1, "Ally");
        assertThat(ally.getCard().isToken()).isTrue();
        assertThat(ally.getCard().getPower()).isEqualTo(1);
        assertThat(ally.getCard().getToughness()).isEqualTo(1);
        assertThat(ally.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(ally.getCard().getSubtypes()).contains(CardSubtype.ALLY);
    }

    @Test
    @DisplayName("Enter trigger creates the token even after Kyoshi Warriors dies")
    void triggerResolvesAfterSourceDies() {
        harness.setHand(player1, List.of(new KyoshiWarriors()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        assertThat(countPermanents(player1, "Ally")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Ally")).isZero();

        Permanent warriors = findPermanent(player1, "Kyoshi Warriors");
        warriors.setMarkedDamage(gqs.getEffectiveToughness(gd, warriors));
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Kyoshi Warriors");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Ally")).isEqualTo(1);
        assertThat(countPermanents(player2, "Ally")).isZero();
    }
}
