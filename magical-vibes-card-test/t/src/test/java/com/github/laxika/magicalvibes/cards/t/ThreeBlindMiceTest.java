package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreeBlindMice.class, ToughCookie.class, Opalescence.class})
class ThreeBlindMiceTest extends BaseCardTest {

    @Test
    void chapterICreatesAWhiteMouseToken() {
        castAndResolveSaga();

        List<Permanent> mice = findPermanents(player1, "Mouse");
        assertThat(mice).hasSize(1);
        assertThat(mice.get(0).getCard().isToken()).isTrue();
        assertThat(mice.get(0).getCard().getPower()).isEqualTo(1);
        assertThat(mice.get(0).getCard().getToughness()).isEqualTo(1);
    }

    @Test
    void chaptersIIAndIIICopyAControlledToken() {
        castAndResolveSaga();
        Permanent saga = findPermanent(player1, "Three Blind Mice");
        Permanent mouse = findPermanent(player1, "Mouse");

        saga.setCounterCount(CounterType.LORE, 1);
        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(mouse.getId());
        harness.handlePermanentChosen(player1, mouse.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mouse")).hasSize(2);

        saga = findPermanent(player1, "Three Blind Mice");
        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();
        Permanent copiedMouse = findPermanents(player1, "Mouse").get(1);
        harness.handlePermanentChosen(player1, copiedMouse.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mouse")).hasSize(3);
    }

    @Test
    void chapterIVBoostsCreaturesAndGrantsVigilanceUntilEndOfTurn() {
        harness.addToBattlefield(player1, new ThreeBlindMice());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ToughCookie());
        Permanent saga = findPermanent(player1, "Three Blind Mice");
        saga.setCounterCount(CounterType.LORE, 3);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    void chapterIICanCopyANoncreatureFoodToken() {
        harness.castFromHand(player1, new ToughCookie(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent food = findPermanent(player1, "Food");
        castAndResolveSaga();

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(food.getId());
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(2);
        Permanent copy = findPermanents(player1, "Food").get(1);
        assertThat(gqs.isCreature(gd, copy)).isFalse();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(copy);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void copyChaptersExcludeOpponentsTokensAndNontokenPermanents() {
        harness.castFromHand(player2, new ThreeBlindMice(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new ToughCookie());
        castAndResolveSaga();
        Permanent mouse = findPermanent(player1, "Mouse");

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(mouse.getId());
        harness.handlePermanentChosen(player1, mouse.getId());
        harness.passBothPriorities();
        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactlyInAnyOrderElementsOf(findPermanents(player1, "Mouse").stream()
                        .map(Permanent::getId).toList());
    }

    @Test
    void chapterIIDoesNotCopyATokenThatLeavesBeforeResolution() {
        castAndResolveSaga();
        Permanent mouse = findPermanent(player1, "Mouse");
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, mouse.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mouse);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mouse")).isEmpty();
        harness.assertOnBattlefield(player1, "Three Blind Mice");
    }

    @Test
    void chapterIVExpiresAndDoesNotAffectOpponentsOrLaterCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ThreeBlindMice());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ToughCookie());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ToughCookie());
        saga.setCounterCount(CounterType.LORE, 3);
        advanceToNextChapter();
        harness.assertOnBattlefield(player1, "Three Blind Mice");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Three Blind Mice");
        harness.assertInGraveyard(player1, "Three Blind Mice");
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, own, Keyword.VIGILANCE)).isTrue();
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();
        Permanent later = harness.addToBattlefieldAndReturn(player1, new ToughCookie());
        assertThat(later.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, later, Keyword.VIGILANCE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(own.getPowerModifier()).isZero();
        assertThat(own.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, own, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void chapterIVIncludesTheSagaItselfWhenItIsACreature() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ThreeBlindMice());
        saga.setCounterCount(CounterType.LORE, 3);
        advanceToNextChapter();
        // Remove a lore counter in response so the Saga remains after its final chapter resolves.
        saga.setCounterCount(CounterType.LORE, 3);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Three Blind Mice");
        assertThat(gqs.isCreature(gd, saga)).isTrue();
        assertThat(gqs.getEffectivePower(gd, saga)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, saga, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void copyChaptersRequireATargetWhenAControlledTokenExists() {
        castAndResolveSaga();
        Permanent mouse = findPermanent(player1, "Mouse");
        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPlayerIds())
                .isEmpty();
        harness.handlePermanentChosen(player1, mouse.getId());
        harness.passBothPriorities();
        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPlayerIds())
                .isEmpty();
    }

    @Test
    void copyChaptersWithoutControlledTokensCreateNothing() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ThreeBlindMice());
        saga.setCounterCount(CounterType.LORE, 1);
        advanceToNextChapter();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Mouse")).isEmpty();
        advanceToNextChapter();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Mouse")).isEmpty();
        harness.assertOnBattlefield(player1, "Three Blind Mice");
    }

    private void castAndResolveSaga() {
        harness.castFromHand(player1, new ThreeBlindMice(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
