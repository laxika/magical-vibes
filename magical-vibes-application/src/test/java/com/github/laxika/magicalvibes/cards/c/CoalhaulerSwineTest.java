package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.cards.v.VotaryOfTheConclave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoalhaulerSwine.class, Char.class, VotaryOfTheConclave.class})
class CoalhaulerSwineTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage makes Coalhauler Swine deal that much damage to each player")
    void nonCombatDamageHitsEachPlayer() {
        Permanent swine = addCreatureReady(player2, new CoalhaulerSwine());
        harness.setHand(player1, List.of(new Char()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, swine.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player2, "Coalhauler Swine");
    }

    @Test
    @DisplayName("Combat damage makes Coalhauler Swine deal that much damage to each player")
    void combatDamageHitsEachPlayer() {
        Permanent attacker = addCreatureReady(player1, new VotaryOfTheConclave());
        Permanent swine = addCreatureReady(player2, new CoalhaulerSwine());

        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player2, "Coalhauler Swine");
    }
}
