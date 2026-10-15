package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GogoMysteriousMime.class, SolemnSimulacrum.class})
class GogoMysteriousMimeTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat offers another creature you control as the copy target")
    void offersAnotherCreatureYouControl() {
        Permanent gogo = addGogo();
        Permanent ownCreature = addCreatureReady(player1, new SolemnSimulacrum());
        Permanent opponentCreature = addCreatureReady(player2, new SolemnSimulacrum());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownCreature.getId())
                .doesNotContain(gogo.getId(), opponentCreature.getId());
    }

    @Test
    @DisplayName("Accepting the copy gives Gogo and the target the temporary bonuses")
    void acceptingCopyGivesBothCreaturesBonuses() {
        Permanent gogo = addGogo();
        Permanent target = addCreatureReady(player1, new SolemnSimulacrum());

        resolveChoice(target);

        assertThat(gogo.getCard().getName()).isEqualTo("Gogo, Mysterious Mime");
        assertThat(gqs.getEffectivePower(gd, gogo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gogo)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, gogo, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gogo.isMustAttackThisTurn()).isTrue();
        assertThat(target.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Declining the copy leaves Gogo and the target unchanged")
    void decliningCopyLeavesBothCreaturesUnchanged() {
        Permanent gogo = addGogo();
        Permanent target = addCreatureReady(player1, new SolemnSimulacrum());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gogo.getCard().getName()).isEqualTo("Gogo, Mysterious Mime");
        assertThat(gqs.hasKeyword(gd, gogo, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gogo.isMustAttackThisTurn()).isFalse();
        assertThat(target.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Copy and temporary bonuses wear off at end of turn")
    void copyAndBonusesWearOffAtEndOfTurn() {
        Permanent gogo = addGogo();
        Permanent target = addCreatureReady(player1, new SolemnSimulacrum());

        resolveChoice(target);
        assertThat(gogo.getCard().getName()).isEqualTo("Gogo, Mysterious Mime");
        assertThat(gogo.isMustAttackThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.ensurePriority(player1);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gogo.getCard().getName()).isEqualTo("Gogo, Mysterious Mime");
        assertThat(gqs.hasKeyword(gd, gogo, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, gogo)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gogo.isMustAttackThisTurn()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(target.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Gogo does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        addGogo();
        addCreatureReady(player1, new SolemnSimulacrum());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("No copy target is offered when Gogo is the only creature you control")
    void doesNothingWithoutAnotherCreature() {
        Permanent gogo = addGogo();
        addCreatureReady(player2, new SolemnSimulacrum());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, gogo, Keyword.HASTE)).isFalse();
        assertThat(gogo.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Copying preserves Gogo's counters and does not copy the target's counters or tapped state")
    void copiesOnlyCopiableValues() {
        Permanent gogo = addGogo();
        gogo.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player1, new SolemnSimulacrum());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        target.tap();

        resolveChoice(target);

        assertThat(gqs.getEffectivePower(gd, gogo)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, gogo)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gogo.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An absent target prevents both the copy and all bonuses")
    void absentTargetPreventsCopyAndBonuses() {
        Permanent gogo = addGogo();
        Permanent target = addCreatureReady(player1, new SolemnSimulacrum());
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gogo)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, gogo, Keyword.HASTE)).isFalse();
        assertThat(gogo.isMustAttackThisTurn()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Gogo leaving before resolution prevents the conditional bonuses to the target")
    void absentGogoCannotPayOptionalCopyCost() {
        Permanent gogo = addGogo();
        Permanent target = addCreatureReady(player1, new SolemnSimulacrum());
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, gogo));

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(target.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Copying a face-down creature uses its face-down characteristics")
    void copiesFaceDownCharacteristics() {
        Permanent gogo = addGogo();
        Permanent target = addCreatureReady(player1, new SolemnSimulacrum());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        target.setManifested(true);

        resolveChoice(target);

        assertThat(gogo.isFaceDown()).isFalse();
        assertThat(gogo.getCard().getName()).isEqualTo("Gogo, Mysterious Mime");
        assertThat(gqs.isArtifact(gd, gogo)).isFalse();
        assertThat(gqs.getEffectivePower(gd, gogo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gogo)).isEqualTo(2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, gogo));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Gogo gains the copied creature's death ability without entering the battlefield again")
    void gainsCopiedDeathAbility() {
        Permanent gogo = addGogo();
        Permanent target = addCreatureReady(player1, new SolemnSimulacrum());
        SolemnSimulacrum drawCard = new SolemnSimulacrum();
        harness.setLibrary(player1, List.of(drawCard));
        harness.setHand(player1, List.of());

        resolveChoice(target);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, gogo));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawCard);
        harness.assertInGraveyard(player1, "Gogo, Mysterious Mime");
    }

    @Test
    @DisplayName("Gogo loses the copied death ability when the copy expires")
    void losesCopiedDeathAbilityAtCleanup() {
        Permanent gogo = addGogo();
        Permanent target = addCreatureReady(player1, new SolemnSimulacrum());
        resolveChoice(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.ensurePriority(player1);
        harness.passUntil(TurnStep.CLEANUP);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, gogo));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addGogo() {
        return addCreatureReady(player1, new GogoMysteriousMime());
    }

    private void resolveChoice(Permanent target) {
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
