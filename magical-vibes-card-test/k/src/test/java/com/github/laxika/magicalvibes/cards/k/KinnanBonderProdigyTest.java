package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.p.ParadiseDruid;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SimicSignet;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KinnanBonderProdigy.class, SolRing.class, ParadiseDruid.class,
        LlanowarElves.class, GrizzlyBears.class, Forest.class, Shock.class,
        Humble.class, SimicSignet.class, LotusPetal.class})
class KinnanBonderProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a nonland permanent for mana adds one more mana of its type")
    void tappingNonlandPermanentForManaAddsMana() {
        harness.addToBattlefield(player1, new KinnanBonderProdigy());
        Permanent solRing = harness.addToBattlefieldAndReturn(player1, new SolRing());

        harness.activateAbility(player1, 1, null, null);

        assertThat(solRing.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("The bonus follows the color chosen by an any-color permanent")
    void tappingAnyColorPermanentAddsChosenColor() {
        harness.addToBattlefield(player1, new KinnanBonderProdigy());
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ParadiseDruid());
        druid.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability offers only non-Human creatures from the top five")
    void offersNonHumanCreature() {
        harness.addToBattlefield(player1, new KinnanBonderProdigy());
        harness.setLibrary(player1, List.of(
                new KinnanBonderProdigy(), new LlanowarElves(), new GrizzlyBears(),
                new Forest(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice search =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(search).isNotNull();
        assertThat(search.allCards().stream().filter(card -> search.validCardIds().contains(card.getId())))
                .extracting(Card::getName)
                .containsExactly("Llanowar Elves", "Grizzly Bears");

        harness.handleMultipleCardsChosen(player1, List.of(search.validCardIds().getFirst()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kinnan, Bonder Prodigy", "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"GREEN", "BLUE"})
    @DisplayName("A permanent producing two types lets Kinnan's controller choose either bonus type")
    void choosesBonusFromAllProducedTypes(ManaColor chosenColor) {
        harness.addToBattlefield(player1, new KinnanBonderProdigy());
        harness.addToBattlefield(player1, new SimicSignet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, chosenColor.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(chosenColor)).isEqualTo(2);
        ManaColor otherColor = chosenColor == ManaColor.GREEN ? ManaColor.BLUE : ManaColor.GREEN;
        assertThat(gd.playerManaPools.get(player1.getId()).get(otherColor)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Kinnan does not produce bonus mana after losing all abilities")
    void losingAbilitiesDisablesManaTrigger() {
        Permanent kinnan = harness.addToBattlefieldAndReturn(player1, new KinnanBonderProdigy());
        harness.addToBattlefield(player1, new SolRing());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, kinnan.getId());

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing the nonland source as a mana ability cost still triggers Kinnan")
    void sacrificedManaSourceStillAddsBonus() {
        harness.addToBattlefield(player1, new KinnanBonderProdigy());
        harness.addToBattlefield(player1, new LotusPetal());

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        harness.assertInGraveyard(player1, "Lotus Petal");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping a land for mana does not trigger Kinnan")
    void landDoesNotAddBonus() {
        harness.addToBattlefield(player1, new KinnanBonderProdigy());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent tapping a nonland mana source does not trigger your Kinnan")
    void opponentManaSourceDoesNotAddBonus() {
        harness.addToBattlefield(player1, new KinnanBonderProdigy());
        harness.addToBattlefield(player2, new SolRing());

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("You may decline an eligible creature and put all five looked-at cards on the bottom")
    void decliningCreaturePreservesUnlookedCardsOnTop() {
        harness.addToBattlefield(player1, new KinnanBonderProdigy());
        List<Card> lookedAt = List.of(new GrizzlyBears(), new LlanowarElves(),
                new KinnanBonderProdigy(), new Forest(), new Shock());
        Card sixth = new Forest();
        List<Card> library = new ArrayList<>(lookedAt);
        library.add(sixth);
        harness.setLibrary(player1, library);
        activateLibraryAbility();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(sixth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature below the top five cannot be selected and stays above the bottomed cards")
    void noEligibleCardsDoesNotLookAtSixthCard() {
        harness.addToBattlefield(player1, new KinnanBonderProdigy());
        List<Card> lookedAt = List.of(new KinnanBonderProdigy(), new Forest(),
                new Shock(), new Forest(), new Shock());
        Card sixth = new GrizzlyBears();
        List<Card> library = new ArrayList<>(lookedAt);
        library.add(sixth);
        harness.setLibrary(player1, library);

        activateLibraryAbility();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(sixth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The ability works with fewer than five cards and puts the chosen creature in untapped")
    void shortLibraryCanPutCreatureOntoBattlefield() {
        harness.addToBattlefield(player1, new KinnanBonderProdigy());
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(bears, forest));
        activateLibraryAbility();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bears.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.isTapped()).isFalse();
        assertThat(entered.isSummoningSick()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library produces no choice or battlefield addition")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new KinnanBonderProdigy());
        harness.setLibrary(player1, List.of());

        activateLibraryAbility();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private void activateLibraryAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
