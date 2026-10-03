package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.cards.f.FieryConclusion;
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

@CardUsed({CoalhaulerSwine.class, Char.class, FieryConclusion.class, VotaryOfTheConclave.class})
class CoalhaulerSwineTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage makes Coalhauler Swine deal that much damage to each player")
    void nonCombatDamageHitsEachPlayer() {
        Permanent swine = addCreatureReady(player2, new CoalhaulerSwine());
        harness.setHand(player1, List.of(new Char()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, swine.getId());
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

    @Test
    @DisplayName("Both Swines trigger when they deal lethal combat damage to each other")
    void simultaneousLethalCombatDamageTriggersBothSwines() {
        Permanent attacker = addCreatureReady(player1, new CoalhaulerSwine());
        addCreatureReady(player2, new CoalhaulerSwine());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 12);
        harness.assertInGraveyard(player1, "Coalhauler Swine");
        harness.assertInGraveyard(player2, "Coalhauler Swine");
    }

    @Test
    @DisplayName("Damage from its controller's spell still makes the Swine damage each player")
    void controllersSpellTriggersSwine() {
        Permanent swine = addCreatureReady(player1, new CoalhaulerSwine());
        harness.setHand(player1, List.of(new Char()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, swine.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Coalhauler Swine");
    }

    @Test
    @DisplayName("Damage exceeding the Swine's toughness is reflected in full")
    void excessLethalDamageIsNotCappedAtToughness() {
        Permanent sacrifice = addCreatureReady(player1, new VotaryOfTheConclave());
        Permanent swine = addCreatureReady(player2, new CoalhaulerSwine());
        harness.setHand(player1, List.of(new FieryConclusion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, swine.getId(), sacrifice.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Coalhauler Swine");
    }
}
