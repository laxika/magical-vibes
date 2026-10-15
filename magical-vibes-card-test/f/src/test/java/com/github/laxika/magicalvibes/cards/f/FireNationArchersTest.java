package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireNationArchers.class})
class FireNationArchersTest extends BaseCardTest {

    @Test
    @DisplayName("Activation deals 2 damage to each opponent and creates a 2/2 red Soldier")
    void activationDamagesEachOpponentAndCreatesSoldier() {
        addCreatureReady(player1, new FireNationArchers());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Soldier")).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("A tapped, summoning-sick Archer can activate without tapping or dealing damage to its controller")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent archers = harness.addToBattlefieldAndReturn(player1, new FireNationArchers());
        archers.setSummoningSick(true);
        archers.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(archers.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Soldier")).singleElement().satisfies(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(token.isTapped()).isFalse();
        });
    }

    @Test
    @DisplayName("Each paid activation deals damage and creates another Soldier without tapping the Archer")
    void canActivateRepeatedly() {
        Permanent archers = addCreatureReady(player1, new FireNationArchers());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        assertThat(archers.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The second player's activation damages the first player and creates the token for the second player")
    void usesAbilityControllerForOpponentAndToken() {
        addCreatureReady(player2, new FireNationArchers());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player2, "Soldier")).isEqualTo(1);
        assertThat(countPermanents(player1, "Soldier")).isZero();
    }
}
