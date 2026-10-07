package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderingTanadon.class})
class ThunderingTanadonTest extends BaseCardTest {

    @ParameterizedTest
    @CsvSource({"0, 16", "1, 18", "2, 20"})
    void castsWithEachCombinationOfGreenManaAndLife(int greenMana, int expectedLife) {
        harness.setHand(player1, List.of(new ThunderingTanadon()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, greenMana);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thundering Tanadon");
        harness.assertLife(player1, expectedLife);
    }

    @Test
    void canSpendGreenManaOnGenericCostAndPayLifeForBothSymbols() {
        harness.setHand(player1, List.of(new ThunderingTanadon()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thundering Tanadon");
        harness.assertLife(player1, 16);
    }

    @Test
    void cannotPayBothSymbolsWithOnlyThreeLife() {
        harness.setHand(player1, List.of(new ThunderingTanadon()));
        harness.setLife(player1, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Thundering Tanadon");
        harness.assertLife(player1, 3);
    }

    @Test
    void lifePaymentDoesNotReplaceGenericMana() {
        harness.setHand(player1, List.of(new ThunderingTanadon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Thundering Tanadon");
        harness.assertLife(player1, 20);
    }

    @Test
    void tramplesOverBlockerEvenWhenBothCreaturesDie() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ThunderingTanadon());
        attacker.setSummoningSick(false);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ThunderingTanadon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Thundering Tanadon");
        harness.assertInGraveyard(player2, "Thundering Tanadon");
    }
}
