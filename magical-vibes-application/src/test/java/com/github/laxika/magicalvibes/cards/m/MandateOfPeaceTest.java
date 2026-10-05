package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IllusionistsGambit;
import com.github.laxika.magicalvibes.cards.s.Silence;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MandateOfPeace.class, GrizzlyBears.class, Silence.class, IllusionistsGambit.class})
class MandateOfPeaceTest extends BaseCardTest {

    @Test
    @DisplayName("Ends combat, exiles the stack, and silences opponents")
    void endsCombatAndExilesStack() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        harness.setHand(player2, List.of(new Silence()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0);

        harness.castFromHand(player1, new MandateOfPeace(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(attacker.isAttacking()).isFalse();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playersSilencedThisTurn).containsOnly(player2.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mandate of Peace"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Silence"));

        harness.setHand(player2, List.of(new Silence()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be cast outside combat")
    void cannotBeCastOutsideCombat() {
        harness.setHand(player1, List.of(new MandateOfPeace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void canEndCombatBeforeAttackersAndControllerCanStillCastSpells() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new MandateOfPeace(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playersSilencedThisTurn).containsOnly(player2.getId());
        harness.castFromHand(player1, new Silence(), "{W}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Silence");
    }

    @Test
    void endingCombatPreservesTheNextAdditionalCombatPhase() {
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.castFromHand(player2, new IllusionistsGambit(), "{2}{U}{U}");
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        harness.castFromHand(player1, new MandateOfPeace(), "{1}{W}");
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities);

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.playersSilencedThisTurn).containsOnly(player2.getId());
    }

    @Test
    void removesBlockersWithoutDealingCombatDamageOrUntappingAttackers() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.castFromHand(player2, new MandateOfPeace(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(blocker.isBlocking()).isFalse();
        assertThat(attacker.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playersSilencedThisTurn).containsOnly(player1.getId());
    }
}
