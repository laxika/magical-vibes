package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BalefulMastery;
import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Jadzi;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VelomachusLorehold.class, BlasphemousAct.class, Forest.class, GrizzlyBears.class,
        Pyroclasm.class, BalefulMastery.class, Jadzi.class})
class VelomachusLoreholdTest extends BaseCardTest {

    @Test
    void offersOnlyMatchingSpellsWithinItsPower() {
        Permanent velomachus = addCreatureReady(player1, new VelomachusLorehold());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Pyroclasm pyroclasm = new Pyroclasm();
        BlasphemousAct blasphemousAct = new BlasphemousAct();
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(pyroclasm, blasphemousAct, forest, bears));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Pyroclasm");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(velomachus.isTapped()).isFalse();
    }

    @Test
    void castsTheChosenSpellWithoutPayingAndBottomsTheRest() {
        addCreatureReady(player1, new VelomachusLorehold());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Pyroclasm pyroclasm = new Pyroclasm();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(pyroclasm, forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pyroclasm");
    }

    @Test
    void decliningTheSpellBottomsAllLookedAtCards() {
        addCreatureReady(player1, new VelomachusLorehold());
        Pyroclasm pyroclasm = new Pyroclasm();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(pyroclasm, forest));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(pyroclasm, forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(pyroclasm);
    }

    @Test
    void usesPowerAtResolutionRatherThanAtAttack() {
        Permanent velomachus = addCreatureReady(player1, new VelomachusLorehold());
        BlasphemousAct spell = new BlasphemousAct();
        harness.setLibrary(player1, List.of(spell));

        declareAttackers(List.of(0));
        velomachus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(spell);
        harness.handleCardChosen(player1, -1);
    }

    @Test
    void usesLastKnownPowerWhenExiledBeforeTriggerResolves() {
        Permanent velomachus = addCreatureReady(player1, new VelomachusLorehold());
        velomachus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        BlasphemousAct spell = new BlasphemousAct();
        harness.setLibrary(player1, List.of(spell));
        harness.setHand(player2, List.of(new BalefulMastery()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        declareAttackers(List.of(0));
        harness.castAndResolveInstant(player2, 0, velomachus.getId());
        harness.assertNotOnBattlefield(player1, "Velomachus Lorehold");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(spell);
        harness.handleCardChosen(player1, -1);
    }

    @Test
    void offersAnEligibleSorceryBackFaceEvenWhenTheFrontIsACreature() {
        addCreatureReady(player1, new VelomachusLorehold());
        Jadzi card = new Jadzi();
        harness.setLibrary(player1, List.of(card));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(card);
    }

    @Test
    void looksAtOnlySevenCardsAndPutsThemBelowTheUntouchedLibrary() {
        addCreatureReady(player1, new VelomachusLorehold());
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest fourth = new Forest();
        Forest fifth = new Forest();
        Forest sixth = new Forest();
        Forest seventh = new Forest();
        Pyroclasm eighth = new Pyroclasm();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, sixth, seventh, eighth));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(eighth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 8))
                .containsExactlyInAnyOrder(first, second, third, fourth, fifth, sixth, seventh);
    }

    @Test
    void emptyLibraryDoesNotRequireAChoice() {
        addCreatureReady(player1, new VelomachusLorehold());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
