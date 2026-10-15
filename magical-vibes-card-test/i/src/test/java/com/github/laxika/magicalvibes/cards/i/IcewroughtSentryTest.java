package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FrostbridgeGuard;
import com.github.laxika.magicalvibes.cards.s.SuccumbToTheCold;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IcewroughtSentry.class, FrostbridgeGuard.class, SuccumbToTheCold.class})
class IcewroughtSentryTest extends BaseCardTest {

    @Test
    void paidAttackTriggerTapsAnOpponentsCreatureAndBoostsSentry() {
        Permanent sentry = addCreatureReady(player1, new IcewroughtSentry());
        Permanent opponentCreature = addCreatureReady(player2, new FrostbridgeGuard());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).containsExactly(opponentCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(4);
    }

    @Test
    void decliningAttackPaymentDoesNotTapOrBoost() {
        Permanent sentry = addCreatureReady(player1, new IcewroughtSentry());
        Permanent opponentCreature = addCreatureReady(player2, new FrostbridgeGuard());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(3);
    }

    @Test
    void tappingOpponentsCreatureByOpponentsEffectDoesNotBoostSentry() {
        Permanent sentry = addCreatureReady(player1, new IcewroughtSentry());
        Permanent opponentCreature = addCreatureReady(player2, new FrostbridgeGuard());

        opponentCreature.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, opponentCreature, player2.getId()));

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(3);
    }

    @Test
    void paidAttackCreatesATapAbilityThatCanBeRespondedTo() {
        Permanent sentry = addCreatureReady(player1, new IcewroughtSentry());
        Permanent target = addCreatureReady(player2, new FrostbridgeGuard());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(4);
    }

    @Test
    void paidAttackCanTargetAnAlreadyTappedCreatureWithoutBoosting() {
        Permanent sentry = addCreatureReady(player1, new IcewroughtSentry());
        Permanent target = addCreatureReady(player2, new FrostbridgeGuard());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(3);
    }

    @Test
    void anotherCardsTapEffectBoostsSentry() {
        Permanent sentry = addCreatureReady(player1, new IcewroughtSentry());
        addCreatureReady(player1, new FrostbridgeGuard());
        Permanent target = addCreatureReady(player2, new FrostbridgeGuard());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(4);
    }

    @Test
    void tappingOwnCreatureDoesNotBoostSentry() {
        Permanent sentry = addCreatureReady(player1, new IcewroughtSentry());
        addCreatureReady(player1, new FrostbridgeGuard());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, sentry.getId());
        resolveAllTriggers();

        assertThat(sentry.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(3);
    }

    @Test
    void tappingTwoOpposingCreaturesWithOneSpellBoostsTwice() {
        Permanent sentry = addCreatureReady(player1, new IcewroughtSentry());
        Permanent first = addCreatureReady(player2, new FrostbridgeGuard());
        Permanent second = addCreatureReady(player2, new FrostbridgeGuard());
        harness.setHand(player1, List.of(new SuccumbToTheCold()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(5);
    }

    @Test
    void opponentsTapEffectDoesNotBoostSentry() {
        Permanent sentry = addCreatureReady(player1, new IcewroughtSentry());
        addCreatureReady(player2, new FrostbridgeGuard());
        Permanent target = addCreatureReady(player2, new FrostbridgeGuard());
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(3);
    }

    @Test
    void tapBonusExpiresAtEndOfTurn() {
        Permanent sentry = addCreatureReady(player1, new IcewroughtSentry());
        addCreatureReady(player1, new FrostbridgeGuard());
        Permanent target = addCreatureReady(player2, new FrostbridgeGuard());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, target.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(3);
    }
}
