package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.EpharasDispersal;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JacesSanctum;
import com.github.laxika.magicalvibes.cards.p.PhyrexianCensor;
import com.github.laxika.magicalvibes.cards.s.Syncopate;
import com.github.laxika.magicalvibes.cards.t.TheGreatSynthesis;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JinGitaxias.class, TheGreatSynthesis.class, JacesSanctum.class,
        GrizzlyBears.class, HillGiant.class, PhyrexianCensor.class, Forest.class,
        EpharasDispersal.class, Syncopate.class})
class JinGitaxiasTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when its controller casts a noncreature spell with mana value three or greater")
    void drawsForExpensiveNoncreatureSpell() {
        harness.addToBattlefield(player1, new JinGitaxias());
        harness.setHand(player1, List.of(new JacesSanctum()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The activation transforms Jin-Gitaxias when the controller has seven cards in hand")
    void activationTransformsWithSevenCardsInHand() {
        Permanent jin = addCreatureReady(player1, new JinGitaxias());
        harness.setHand(player1, cards(7));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(jin), null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "The Great Synthesis")).isNotNull();

        harness.setHand(player1, cards(8));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Chapter II returns non-Phyrexian creatures but leaves Phyrexians on the battlefield")
    void chapterIIReturnsNonPhyrexianCreatures() {
        Permanent saga = addBackFaceSaga(1);
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent phyrexian = addCreatureReady(player1, new PhyrexianCensor());

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentBear);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(phyrexian);
        assertThat(gd.playerHands.get(player1.getId())).contains(ownBear.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentBear.getCard());
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III can cast multiple spells for free before returning transformed")
    void chapterIIICastsMultipleSpellsThenReturnsTransformed() {
        addBackFaceSaga(2);
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(bears, giant, forest));

        advanceSagaToNextChapter();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .contains("Jin-Gitaxias");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).extracting(entry -> entry.getCard().getName())
                .contains("Grizzly Bears", "Hill Giant");
    }

    @Test
    void doesNotDrawForCreatureSpellsEvenWithManaValueAboveThree() {
        harness.addToBattlefield(player1, new JinGitaxias());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    void doesNotDrawForOpponentsNoncreatureSpell() {
        harness.addToBattlefield(player1, new JinGitaxias());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new EpharasDispersal()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, bear.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bear.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void wardCountersOpponentsSpellWhenTheyCannotPayTwo() {
        Permanent jin = harness.addToBattlefieldAndReturn(player1, new JinGitaxias());
        harness.setHand(player2, List.of(new EpharasDispersal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, jin.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Jin-Gitaxias");
        harness.assertInGraveyard(player2, "Ephara's Dispersal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsChosenXWhenCheckingSpellManaValue() {
        harness.addToBattlefield(player1, new JinGitaxias());
        HillGiant giant = new HillGiant();
        harness.setHand(player1, List.of(giant, new Syncopate()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 2, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotActivateWithOnlySixCardsInHand() {
        harness.addToBattlefield(player1, new JinGitaxias());
        harness.setHand(player1, cards(6));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Jin-Gitaxias");
    }

    @Test
    void cannotActivateOutsideSorceryTiming() {
        harness.addToBattlefield(player1, new JinGitaxias());
        harness.setHand(player1, cards(7));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activationStillResolvesAfterHandDropsBelowSeven() {
        harness.addToBattlefield(player1, new JinGitaxias());
        harness.setHand(player1, cards(7));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "The Great Synthesis");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void chapterIDrawsBasedOnHandSizeAtResolution() {
        addBackFaceSaga(0);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        advanceSagaToNextChapter();
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void sagaDoesNotRemoveHandSizeLimitUnlessChapterIResolved() {
        addBackFaceSaga(1);
        harness.setHand(player1, cards(8));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
    }

    @Test
    void chapterIIIReturnsFrontFaceEvenWhenAllSpellsAreDeclined() {
        addBackFaceSaga(2);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setHand(player1, List.of(bear));
        advanceSagaToNextChapter();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Jin-Gitaxias");
        harness.assertNotOnBattlefield(player1, "The Great Synthesis");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bear);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chapterIIIReturnsFrontFaceWhenHandContainsOnlyLands() {
        addBackFaceSaga(2);
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));
        advanceSagaToNextChapter();

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Jin-Gitaxias");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addBackFaceSaga(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new JinGitaxias());
        saga.setCard(saga.getOriginalCard().getBackFaceCard());
        saga.setTransformed(true);
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceSagaToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passBothPriorities();
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
