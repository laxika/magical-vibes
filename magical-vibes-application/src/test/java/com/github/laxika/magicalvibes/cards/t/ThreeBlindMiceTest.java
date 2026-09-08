package com.github.laxika.magicalvibes.cards.t;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreeBlindMice.class, GrizzlyBears.class})
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent saga = findPermanent(player1, "Three Blind Mice");
        saga.setCounterCount(CounterType.LORE, 3);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.VIGILANCE);
    }

    private void castAndResolveSaga() {
        harness.setHand(player1, List.of(new ThreeBlindMice()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
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
