package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheWarInHeaven.class, GrizzlyBears.class, HillGiant.class, GiantGrowth.class})
class TheWarInHeavenTest extends BaseCardTest {

    @Test
    void chapterOneDrawsThreeAndLosesThreeLife() {
        harness.setLibrary(player1, List.of(new GiantGrowth(), new GiantGrowth(), new GiantGrowth()));
        harness.setHand(player1, List.of(new TheWarInHeaven()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    void chapterThreeReturnsUpToThreeCreaturesWithinManaValueAndMakesThemArtifactNecrodermisCreatures() {
        Card bears = new GrizzlyBears();
        Card secondBears = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        harness.setGraveyard(player1, List.of(bears, secondBears, hillGiant));

        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheWarInHeaven());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.maxTotalManaValue()).isEqualTo(8);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), secondBears.getId(), hillGiant.getId()));
        harness.passBothPriorities();

        List<Permanent> returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> List.of("Grizzly Bears", "Hill Giant")
                        .contains(permanent.getCard().getName()))
                .toList();
        assertThat(returned).hasSize(3);
        assertThat(returned).allSatisfy(permanent -> {
            assertThat(gqs.isArtifact(gd, permanent)).isTrue();
            assertThat(permanent.getCounterCount(CounterType.NECRODERMIS)).isEqualTo(1);
        });
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears")
                        || card.getName().equals("Hill Giant"));
    }

    @Test
    void chapterTwoMillsThreeCardsOnlyForItsController() {
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        Card third = new GiantGrowth();
        Card remaining = new GiantGrowth();
        harness.setLibrary(player1, List.of(first, second, third, remaining));
        harness.setLibrary(player2, List.of(new GiantGrowth()));

        triggerChapter(2);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void chapterThreeCannotReturnFourCreaturesEvenWithinEightManaValue() {
        List<Card> creatures = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, creatures);

        triggerChapter(3);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                creatures.stream().map(Card::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chapterThreeAllowsChoosingNoCreaturesAndThenSacrificesSaga() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        triggerChapter(3);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "The War in Heaven");
        harness.assertInGraveyard(player1, "The War in Heaven");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void chapterThreeRejectsCreaturesWithTotalManaValueAboveEight() {
        List<Card> creatures = List.of(new HillGiant(), new HillGiant(), new GrizzlyBears());
        harness.setGraveyard(player1, creatures);

        triggerChapter(3);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                creatures.stream().map(Card::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chapterThreeCannotChooseNoncreaturesOrOpponentsCreatures() {
        Card ownCreature = new GrizzlyBears();
        Card noncreature = new GiantGrowth();
        Card opponentsCreature = new HillGiant();
        harness.setGraveyard(player1, List.of(ownCreature, noncreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));

        triggerChapter(3);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opponentsCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentsCreature);
    }

    private void triggerChapter(int chapter) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheWarInHeaven());
        saga.setCounterCount(CounterType.LORE, chapter - 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
