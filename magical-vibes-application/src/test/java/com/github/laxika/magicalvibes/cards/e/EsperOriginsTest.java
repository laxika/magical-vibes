package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.t.TravelingChocobo;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SummonEsperMaduin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EsperOrigins.class, SummonEsperMaduin.class, Forest.class, TravelingChocobo.class, Clone.class})
class EsperOriginsTest extends BaseCardTest {

    @Test
    void normalCastSurveilsGainsLifeAndGoesToGraveyard() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new EsperOrigins()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Esper Origins");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void flashbackTransformsTheSpellWithAFinalityCounter() {
        harness.setLibrary(player1, List.of());
        EsperOrigins card = new EsperOrigins();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SummonEsperMaduin)
                .findFirst()
                .orElseThrow();
        assertThat(saga.isTransformed()).isTrue();
        assertThat(saga.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        harness.assertNotInGraveyard(player1, "Esper Origins");
        harness.assertLife(player1, 22);
    }

    @Test
    void firstChapterRevealsPermanentToHand() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonEsperMaduin());
        saga.setCounterCount(CounterType.LORE, 0);
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void thirdChapterBoostsAndGivesTrampleToOtherCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonEsperMaduin());
        saga.setCounterCount(CounterType.LORE, 2);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new TravelingChocobo());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TravelingChocobo());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void surveilCanKeepOneCardAndPutTheOtherInTheGraveyardBeforeGainingLife() {
        Forest kept = new Forest();
        EsperOrigins milled = new EsperOrigins();
        harness.setLibrary(player1, List.of(milled, kept));
        harness.setHand(player1, List.of(new EsperOrigins()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(milled, kept);
        harness.assertLife(player1, 20);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled);
        harness.assertLife(player1, 22);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void firstChapterLeavesANonpermanentOnTop() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonEsperMaduin());
        saga.setCounterCount(CounterType.LORE, 0);
        EsperOrigins top = new EsperOrigins();
        harness.setLibrary(player1, List.of(top));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top);
    }

    @Test
    void secondChapterAddsTwoGreenMana() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonEsperMaduin());
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    void transformedSagaCannotAttackOnTheTurnItEnters() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new EsperOrigins()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void finalChapterSacrificeExilesTheOriginalCardWithItsFinalityCounter() {
        harness.setLibrary(player1, List.of());
        EsperOrigins original = new EsperOrigins();
        harness.setGraveyard(player1, List.of(original));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent saga = gd.playerBattlefields.get(player1.getId()).getFirst();
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(original);
        harness.assertNotInGraveyard(player1, "Esper Origins");
    }

    @Test
    void copyingTheTransformedSagaDoesNotAddAFinalityCounter() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new EsperOrigins()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent saga = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, saga.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof Clone)
                .findFirst().orElseThrow();
        assertThat(copy.getCounterCount(CounterType.FINALITY)).isZero();
        assertThat(saga.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }
}
