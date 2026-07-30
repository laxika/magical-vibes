package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarjhanTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed when controller controls no Islands")
    void sacrificedWhenNoIslands() {
        harness.setHand(player1, List.of(new Marjhan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> state trigger fires
        harness.passBothPriorities(); // resolve state trigger -> sacrificed

        harness.assertNotOnBattlefield(player1, "Marjhan");
        harness.assertInGraveyard(player1, "Marjhan");
    }

    @Test
    @DisplayName("Survives while controller controls an Island")
    void survivesWithIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new Marjhan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Marjhan");
    }

    @Test
    @DisplayName("Tapped Marjhan does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        harness.addToBattlefield(player1, new Island());
        Permanent marjhan = addMarjhan(player1, true);

        advanceToNextTurn(player2);

        assertThat(marjhan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a creature during upkeep untaps Marjhan")
    void sacrificingUntapsMarjhanDuringUpkeep() {
        harness.addToBattlefield(player1, new Island());
        Permanent marjhan = addMarjhan(player1, true);
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 2);

        int marjhanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marjhan);
        harness.activateAbility(player1, marjhanIndex, 0, null, null);
        harness.handlePermanentChosen(player1, elves.getId());
        harness.passBothPriorities();

        assertThat(marjhan.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("The untap ability cannot be activated outside the controller's upkeep")
    void untapAbilityRestrictedToUpkeep() {
        harness.addToBattlefield(player1, new Island());
        Permanent marjhan = addMarjhan(player1, true);
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);

        int marjhanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marjhan);
        assertThatThrownBy(() -> harness.activateAbility(player1, marjhanIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(marjhan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{U}{U} shrinks Marjhan and deals 1 damage to an attacking creature without flying")
    void damagesAttackingNonFlyer() {
        harness.addToBattlefield(player1, new Island());
        Permanent marjhan = addMarjhan(player1, false);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);

        declareAttack(bears);

        int marjhanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marjhan);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, marjhanIndex, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(marjhan.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The damage ability cannot target an attacking creature with flying")
    void cannotTargetFlyer() {
        harness.addToBattlefield(player1, new Island());
        Permanent marjhan = addMarjhan(player1, false);
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        hawk.setSummoningSick(false);

        declareAttack(hawk);

        int marjhanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marjhan);
        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, marjhanIndex, 1, null, hawk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());

        Permanent marjhan = addMarjhan(player1, false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int marjhanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marjhan);
        gs.declareAttackers(gd, player1, List.of(marjhanIndex));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWhenDefenderHasNoIsland() {
        harness.addToBattlefield(player1, new Island());

        Permanent marjhan = addMarjhan(player1, false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int marjhanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marjhan);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(marjhanIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void declareAttack(Permanent attacker) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player2, List.of(index));
    }

    private Permanent addMarjhan(Player player, boolean tapped) {
        Permanent perm = new Permanent(new Marjhan());
        perm.setSummoningSick(false);
        if (tapped) {
            perm.tap();
        }
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
        harness.passBothPriorities(); // CLEANUP -> next turn (untap)
    }
}
