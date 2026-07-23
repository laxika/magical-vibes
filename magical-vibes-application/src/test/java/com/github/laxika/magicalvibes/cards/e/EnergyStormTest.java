package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.OpenFire;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnergyStormTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }

    @Test
    @DisplayName("Prevents Shock damage to a player")
    void preventsInstantDamageToPlayer() {
        harness.addToBattlefield(player1, new EnergyStorm());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevents Open Fire damage to a creature")
    void preventsInstantDamageToCreature() {
        harness.addToBattlefield(player1, new EnergyStorm());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new OpenFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(giant);
        assertThat(giant.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent combat damage")
    void doesNotPreventCombatDamage() {
        harness.addToBattlefield(player1, new EnergyStorm());
        Permanent bears = addReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Flying creatures stay tapped; non-fliers untap")
    void flyingCreaturesDontUntap() {
        addReady(player1, new EnergyStorm());
        Permanent flier = addReady(player1, new AirElemental());
        Permanent bears = addReady(player1, new GrizzlyBears());
        flier.tap();
        bears.tap();

        advanceToNextTurn(player2);

        assertThat(flier.isTapped()).isTrue();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Energy Storm")
    void paysCumulativeUpkeep() {
        Permanent storm = harness.addToBattlefieldAndReturn(player1, new EnergyStorm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(storm.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(storm);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Energy Storm")
    void declineSacrifices() {
        Permanent storm = harness.addToBattlefieldAndReturn(player1, new EnergyStorm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(storm);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Energy Storm"));
    }
}
