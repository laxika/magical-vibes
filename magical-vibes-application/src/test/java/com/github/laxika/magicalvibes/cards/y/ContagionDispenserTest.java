package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.b.BloomHulk;
import com.github.laxika.magicalvibes.cards.c.Cankerbloom;
import com.github.laxika.magicalvibes.cards.c.ContagionClasp;
import com.github.laxika.magicalvibes.cards.c.ContagionEngine;
import com.github.laxika.magicalvibes.cards.c.ContentiousPlan;
import com.github.laxika.magicalvibes.cards.e.EvolutionSage;
import com.github.laxika.magicalvibes.cards.f.FluxChanneler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InexorableTide;
import com.github.laxika.magicalvibes.cards.m.MerfolkSkydiver;
import com.github.laxika.magicalvibes.cards.p.PollenbrightDruid;
import com.github.laxika.magicalvibes.cards.r.RoaleskApexHybrid;
import com.github.laxika.magicalvibes.cards.s.SmellFear;
import com.github.laxika.magicalvibes.cards.s.SteadyProgress;
import com.github.laxika.magicalvibes.cards.s.SwordOfTruthAndJustice;
import com.github.laxika.magicalvibes.cards.t.TezzeretsGambit;
import com.github.laxika.magicalvibes.cards.t.Thrummingbird;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContagionDispenser.class, BloomHulk.class, Cankerbloom.class,
        ContagionClasp.class, ContagionEngine.class, ContentiousPlan.class,
        EvolutionSage.class, FluxChanneler.class, InexorableTide.class,
        MerfolkSkydiver.class, PollenbrightDruid.class, RoaleskApexHybrid.class,
        SmellFear.class, SwordOfTruthAndJustice.class, TezzeretsGambit.class,
        Thrummingbird.class, SteadyProgress.class, GrizzlyBears.class})
class ContagionDispenserTest extends BaseCardTest {

    private static final Set<String> SPELLBOOK = Set.of(
            "Bloom Hulk", "Cankerbloom", "Contagion Clasp", "Contagion Engine",
            "Contentious Plan", "Evolution Sage", "Flux Channeler", "Inexorable Tide",
            "Merfolk Skydiver", "Pollenbright Druid", "Roalesk, Apex Hybrid", "Smell Fear",
            "Sword of Truth and Justice", "Tezzeret's Gambit", "Thrummingbird");

    @Test
    void entersAndOffersThreeCardsFromItsSpellbook() {
        harness.setHand(player1, List.of(new ContagionDispenser()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards()).extracting(Card::getName).allMatch(SPELLBOOK::contains);

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
    }

    @Test
    void draftsOnlyOncePerTurnAfterProliferatingDuringItsControllersTurn() {
        harness.addToBattlefield(player1, new ContagionDispenser());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new SteadyProgress(), new SteadyProgress()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId()));
        resolveAllTriggers();
        PendingInteraction.SpellbookDraftChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(firstChoice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(firstChoice.cards().getFirst().getId()));
        resolveAllTriggers();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNull();
    }

    @Test
    void doesNotDraftWhenTheControllerProliferatesOnAnotherPlayersTurn() {
        harness.addToBattlefield(player1, new ContagionDispenser());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SteadyProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNull();
    }

    @Test
    void enteringProliferatesExistingCountersAndEachDispenserDrafts() {
        Permanent dispenser = harness.addToBattlefieldAndReturn(player1, new ContagionDispenser());
        dispenser.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of(new ContagionDispenser()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(dispenser.getId()));
        resolveAllTriggers();

        assertThat(dispenser.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        PendingInteraction.SpellbookDraftChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(firstChoice).isNotNull();
        Card firstDraft = firstChoice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(firstDraft.getId()));
        resolveAllTriggers();
        PendingInteraction.SpellbookDraftChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(secondChoice).isNotNull();
        Card secondDraft = secondChoice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(secondDraft.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraft, secondDraft);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNull();
    }

    @Test
    void draftsEvenWhenNoEligiblePermanentIsChosen() {
        Permanent dispenser = harness.addToBattlefieldAndReturn(player1, new ContagionDispenser());
        dispenser.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SteadyProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(dispenser.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNotNull();
    }

    @Test
    void doesNotDraftWhenAnOpponentProliferatesDuringTheControllersTurn() {
        harness.addToBattlefield(player1, new ContagionDispenser());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new SteadyProgress()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
