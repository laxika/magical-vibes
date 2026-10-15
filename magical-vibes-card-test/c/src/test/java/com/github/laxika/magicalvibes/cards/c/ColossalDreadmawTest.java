package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BishopsSoldier;
import com.github.laxika.magicalvibes.cards.l.LoomingAltisaur;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColossalDreadmaw.class, BishopsSoldier.class, LoomingAltisaur.class})
class ColossalDreadmawTest extends BaseCardTest {

    @Test
    void tramplesOverBlockerAndSurvives() {
        Permanent blocker = prepareBlockedCombat(new BishopsSoldier());

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 4));

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Bishop's Soldier");
        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
    }

    @Test
    void cannotTrampleBeforeAssigningLethalDamageToBlocker() {
        Permanent blocker = prepareBlockedCombat(new BishopsSoldier());

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 5)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample");

        harness.assertLife(player2, 20);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 4));
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Bishop's Soldier");
    }

    @Test
    void cannotTrampleOverBlockerWithMoreToughnessThanItsPower() {
        Permanent blocker = prepareBlockedCombat(new LoomingAltisaur());

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 5, player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample");

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        harness.assertOnBattlefield(player2, "Looming Altisaur");
    }

    private Permanent prepareBlockedCombat(Card blockingCard) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ColossalDreadmaw());
        Permanent blocker = addCreatureReady(player2, blockingCard);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        return blocker;
    }
}
