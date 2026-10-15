package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DauthiSlayer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SolkanarTheSwampKing.class, DauthiSlayer.class, Squire.class, Swamp.class})
class SolkanarTheSwampKingTest extends BaseCardTest {

    @Test
    @DisplayName("Controller casting a black spell gains 1 life")
    void controllerCastsBlackSpell() {
        harness.addToBattlefield(player1, new SolkanarTheSwampKing());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new DauthiSlayer(), "{B}{B}");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Sol'kanar the Swamp King"));

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("Opponent casting a black spell gains the controller 1 life")
    void opponentCastsBlackSpell() {
        harness.addToBattlefield(player1, new SolkanarTheSwampKing());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player2, new DauthiSlayer(), "{B}{B}");
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    @DisplayName("Casting a nonblack spell does not trigger Sol'kanar")
    void nonblackSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SolkanarTheSwampKing());

        harness.castFromHand(player1, new Squire(), "{1}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("A multicolored black spell also triggers Sol'kanar")
    void multicoloredBlackSpellTriggers() {
        harness.addToBattlefield(player1, new SolkanarTheSwampKing());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new SolkanarTheSwampKing(), "{2}{U}{B}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("Sol'kanar does not trigger for its own casting while off the battlefield")
    void ownCastingDoesNotTrigger() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new SolkanarTheSwampKing(), "{2}{U}{B}{R}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        harness.assertOnBattlefield(player1, "Sol'kanar the Swamp King");
    }

    @Test
    @DisplayName("A life gain trigger survives Sol'kanar leaving the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        SolkanarTheSwampKing solkanar = new SolkanarTheSwampKing();
        harness.addToBattlefield(player1, solkanar);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new DauthiSlayer(), "{B}{B}");
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(solkanar));
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
        harness.assertInGraveyard(player1, "Sol'kanar the Swamp King");
    }

    @Test
    @DisplayName("A Swamp controlled only by the attacker does not prevent blocking")
    void attackersSwampDoesNotPreventBlocking() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent blocker = addCreatureReady(player2, new Squire());
        Permanent attacker = addCreatureReady(player1, new SolkanarTheSwampKing());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackersAndPrepareBlockers(List.of(attackerIndex));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Swampwalk prevents blocking when the defending player controls a Swamp")
    void swampwalkPreventsBlockingWithSwamp() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent blocker = addCreatureReady(player2, new Squire());
        Permanent attacker = addCreatureReady(player1, new SolkanarTheSwampKing());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Swampwalk allows blocking when the defending player controls no Swamp")
    void swampwalkAllowsBlockingWithoutSwamp() {
        Permanent blocker = addCreatureReady(player2, new Squire());
        Permanent attacker = addCreatureReady(player1, new SolkanarTheSwampKing());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
