package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWitchsVanity.class, AirElemental.class, GrizzlyBears.class})
class TheWitchsVanityTest extends BaseCardTest {

    @Test
    void chapterIOnlyTargetsAndDestroysOpponentCreaturesWithManaValueTwoOrLess() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent validTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent expensiveTarget = addCreatureReady(player2, new AirElemental());

        castAndResolveSaga();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(validTarget.getId());
        assertThat(choice.validIds()).doesNotContain(ownCreature.getId(), expensiveTarget.getId());

        harness.handlePermanentChosen(player1, validTarget.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(expensiveTarget);
    }

    @Test
    void chapterIICreatesFoodThatCanBeSacrificedForLife() {
        addSagaWithLoreCounter(1);

        advanceToNextMainPhase();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, foodIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void chapterIIICreatesWickedRoleAttachedToControlledCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addSagaWithLoreCounter(2);

        advanceToNextMainPhase();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        assertThat(choice.validIds()).doesNotContain(
                gd.playerBattlefields.get(player2.getId()).getFirst().getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "The Witch's Vanity");
        Permanent role = findPermanent(player1, "Wicked");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();

        int opponentLife = gd.getLife(player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, role));
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void sagaStillCreatesFoodWhenChapterIHasNoLegalTarget() {
        castAndResolveSaga();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "The Witch's Vanity");

        advanceToNextMainPhase();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    void chapterIIICreatesNoRoleWhenTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addSagaWithLoreCounter(2);
        advanceToNextMainPhase();
        harness.handlePermanentChosen(player1, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wicked")).isZero();
        harness.assertInGraveyard(player1, "The Witch's Vanity");
    }

    @Test
    void newestWickedRoleReplacesOlderRoleAndTriggersLifeLoss() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addSagaWithLoreCounter(2);
        advanceToNextMainPhase();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        Permanent oldRole = findPermanent(player1, "Wicked");

        addSagaWithLoreCounter(2);
        advanceToNextMainPhase();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Wicked")).isEqualTo(1);
        assertThat(findPermanent(player1, "Wicked").getId()).isNotEqualTo(oldRole.getId());
        assertThat(findPermanent(player1, "Wicked").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        harness.assertLife(player2, 19);
    }

    @Test
    void chapterIIIWithoutControlledCreaturesCreatesNoRoleAndSagaIsSacrificed() {
        addCreatureReady(player2, new GrizzlyBears());
        addSagaWithLoreCounter(2);

        advanceToNextMainPhase();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Wicked")).isZero();
        harness.assertInGraveyard(player1, "The Witch's Vanity");
    }

    private void castAndResolveSaga() {
        harness.castFromHand(player1, new TheWitchsVanity(), "{1}{B}");
        harness.passBothPriorities();
    }

    private void addSagaWithLoreCounter(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheWitchsVanity());
        saga.setCounterCount(CounterType.LORE, loreCounters);
    }

    private void advanceToNextMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
