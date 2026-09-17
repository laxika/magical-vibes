package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWorldSpell.class, Forest.class, GrizzlyBears.class, HistoryOfBenalia.class, Shock.class})
class TheWorldSpellTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I may put a non-Saga permanent from the top seven into hand")
    void chapterILooksAtTopSevenForNonSagaPermanent() {
        HistoryOfBenalia sagaCard = new HistoryOfBenalia();
        Shock instant = new Shock();
        GrizzlyBears creature = new GrizzlyBears();
        Forest land = new Forest();
        Shock secondInstant = new Shock();
        Forest secondLand = new Forest();
        GrizzlyBears secondCreature = new GrizzlyBears();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(sagaCard, instant, creature, land, secondInstant,
                secondLand, secondCreature, untouched));
        harness.setHand(player1, List.of(new TheWorldSpell()));
        addWorldSpellMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(
                sagaCard, instant, creature, land, secondInstant, secondLand, secondCreature);
        assertThat(choice.validCardIds()).containsExactly(
                creature.getId(), land.getId(), secondLand.getId(), secondCreature.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                untouched, sagaCard, instant, land, secondInstant, secondLand, secondCreature);
    }

    @Test
    @DisplayName("Chapter I bottoms the top seven without an eligible card")
    void chapterIWithNoNonSagaPermanentDoesNotCreateChoice() {
        HistoryOfBenalia sagaOne = new HistoryOfBenalia();
        Shock shockOne = new Shock();
        HistoryOfBenalia sagaTwo = new HistoryOfBenalia();
        Shock shockTwo = new Shock();
        HistoryOfBenalia sagaThree = new HistoryOfBenalia();
        Shock shockThree = new Shock();
        HistoryOfBenalia sagaFour = new HistoryOfBenalia();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(sagaOne, shockOne, sagaTwo, shockTwo, sagaThree,
                shockThree, sagaFour, untouched));
        harness.setHand(player1, List.of(new TheWorldSpell()));
        addWorldSpellMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                untouched, sagaOne, shockOne, sagaTwo, shockTwo, sagaThree, shockThree, sagaFour);
    }

    @Test
    @DisplayName("Chapter III puts up to two non-Saga permanents from hand onto the battlefield")
    void chapterIIIPutsUpToTwoNonSagaPermanentsFromHand() {
        Permanent saga = new Permanent(new TheWorldSpell());
        gd.playerBattlefields.get(player1.getId()).add(saga);
        saga.setCounterCount(CounterType.LORE, 2);
        GrizzlyBears firstCreature = new GrizzlyBears();
        GrizzlyBears secondCreature = new GrizzlyBears();
        HistoryOfBenalia sagaCard = new HistoryOfBenalia();
        Shock instant = new Shock();
        harness.setHand(player1, List.of(firstCreature, secondCreature, sagaCard, instant));

        triggerNextChapter();
        harness.passBothPriorities();

        PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactly(firstCreature.getId(), secondCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sagaCard, instant);
    }

    private void addWorldSpellMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
