package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.a.Arrest;
import com.github.laxika.magicalvibes.cards.c.ContagionClasp;
import com.github.laxika.magicalvibes.cards.l.LumengridDrake;
import com.github.laxika.magicalvibes.cards.c.CarnifexDemon;
import com.github.laxika.magicalvibes.cards.f.FlightSpellbomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({GenesisWave.class, GrizzlyBears.class, Blaze.class, CarnifexDemon.class,
        FlightSpellbomb.class, Forest.class, AccordersShield.class, Arrest.class,
        ContagionClasp.class, LumengridDrake.class})
class GenesisWaveTest extends BaseCardTest {

    @Test
    void emptyLibraryRevealsNothing() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveSorcery(player1, 0, 4);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Genesis Wave");
    }

    @Test
    void revealsOnlyTopXAndAcceptsManaValueEqualToX() {
        Card spellbomb = new FlightSpellbomb();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(spellbomb, forest));
        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(spellbomb.getId()));

        harness.assertOnBattlefield(player1, "Flight Spellbomb");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void auraSelectedWithAnotherPermanentAttachesToExistingCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new LumengridDrake());
        Card arrest = new Arrest();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(arrest, forest));
        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, 3);
        harness.handleMultipleCardsChosen(player1, List.of(arrest.getId(), forest.getId()));
        harness.handlePermanentChosen(player1, creature.getId());

        harness.assertOnBattlefield(player1, "Arrest");
        assertThat(findPermanent(player1, "Arrest").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void noncreaturePermanentGetsItsEntersTrigger() {
        var creature = harness.addToBattlefieldAndReturn(player2, new LumengridDrake());
        Card clasp = new ContagionClasp();
        harness.setLibrary(player1, List.of(clasp));
        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(clasp.getId()));
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Contagion Clasp");
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void metalcraftTriggerSeesArtifactsEnteringWithCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new LumengridDrake());
        Card drake = new LumengridDrake();
        Card shield1 = new AccordersShield();
        Card shield2 = new AccordersShield();
        Card shield3 = new AccordersShield();
        harness.setLibrary(player1, List.of(drake, shield1, shield2, shield3));
        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveSorcery(player1, 0, 4);
        harness.handleMultipleCardsChosen(player1,
                List.of(drake.getId(), shield1.getId(), shield2.getId(), shield3.getId()));
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lumengrid Drake");
        harness.assertNotOnBattlefield(player2, "Lumengrid Drake");
        harness.assertInHand(player2, "Lumengrid Drake");
    }

    // ===== X=0 reveals nothing =====

    @Test
    @DisplayName("Casting with X=0 reveals no cards")
    void xZeroRevealsNothing() {
        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Should not be awaiting any input
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Puts eligible permanents onto battlefield =====

    @Test
    @DisplayName("Eligible permanent cards can be put onto the battlefield")
    void putsEligiblePermanentsOntoBattlefield() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card spellbomb = new FlightSpellbomb();

        harness.setLibrary(player1, List.of(bears, forest, spellbomb));

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 6); // {3}{G}{G}{G} with X=3

        harness.castAndResolveSorcery(player1, 0, 3);

        // Should be awaiting library reveal choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        // Select all three cards (creature MV 2, land MV 0, artifact MV 1 — all <= 3)
        harness.handleMultipleCardsChosen(player1,
                List.of(bears.getId(), forest.getId(), spellbomb.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Flight Spellbomb");
    }

    // ===== Non-permanents go to graveyard =====

    @Test
    @DisplayName("Instants and sorceries revealed go to graveyard")
    void nonPermanentsGoToGraveyard() {
        Card bears = new GrizzlyBears();
        Card blaze = new Blaze();

        harness.setLibrary(player1, List.of(bears, blaze));

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 5); // X=2

        harness.castAndResolveSorcery(player1, 0, 2);

        // Only bears should be selectable; select it
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        // Blaze (sorcery) should be in graveyard
        harness.assertInGraveyard(player1, "Blaze");
    }

    // ===== Permanents with MV > X go to graveyard =====

    @Test
    @DisplayName("Permanent cards with mana value greater than X go to graveyard")
    void permanentsWithHighMVGoToGraveyard() {
        Card forest = new Forest();          // MV 0 — eligible (0 <= 3)
        Card bears = new GrizzlyBears();     // MV 2 — eligible (2 <= 3)
        Card demon = new CarnifexDemon();    // MV 6 — NOT eligible (6 > 3)

        harness.setLibrary(player1, List.of(forest, bears, demon));

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 6); // X=3

        harness.castAndResolveSorcery(player1, 0, 3);

        // Forest and Bears are eligible, Demon (MV 6) is not
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), bears.getId()));

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Carnifex Demon");
    }

    // ===== Player can choose subset =====

    @Test
    @DisplayName("Player can choose to put only some eligible cards onto the battlefield")
    void playerCanChooseSubset() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();

        harness.setLibrary(player1, List.of(bears, forest));

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 5); // X=2

        harness.castAndResolveSorcery(player1, 0, 2);

        // Only select bears, not forest
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        // Forest was not selected — goes to graveyard
        harness.assertInGraveyard(player1, "Forest");
    }

    // ===== Player can choose zero cards =====

    @Test
    @DisplayName("Player can choose zero cards — all revealed go to graveyard")
    void playerCanChooseZeroCards() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();

        harness.setLibrary(player1, List.of(bears, forest));

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 5); // X=2

        harness.castAndResolveSorcery(player1, 0, 2);

        // Select nothing
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    // ===== No eligible cards — all go straight to graveyard =====

    @Test
    @DisplayName("When no revealed cards are eligible, all go to graveyard without prompting")
    void noEligibleCardsAllToGraveyard() {
        Card blaze1 = new Blaze();
        Card blaze2 = new Blaze();

        harness.setLibrary(player1, List.of(blaze1, blaze2));

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 5); // X=2

        harness.castAndResolveSorcery(player1, 0, 2);

        // No interaction expected — both sorceries go to graveyard
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Blaze");
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Blaze")).count()).isEqualTo(2);
    }

    // ===== Creatures get ETB processing =====

    @Test
    @DisplayName("Creatures put onto battlefield get ETB effects processed")
    void creaturesGetETBProcessing() {
        Card demon = new CarnifexDemon(); // ETB: enters with two -1/-1 counters

        harness.setLibrary(player1, List.of(demon));

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 9); // X=6

        harness.castAndResolveSorcery(player1, 0, 6);

        harness.handleMultipleCardsChosen(player1, List.of(demon.getId()));

        harness.assertOnBattlefield(player1, "Carnifex Demon");

        // Carnifex Demon should have two -1/-1 counters from its ETB
        assertThat(findPermanent(player1, "Carnifex Demon")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    // ===== Library smaller than X =====

    @Test
    @DisplayName("When library has fewer than X cards, reveals all available cards")
    void librarySmallerThanX() {
        Card bears = new GrizzlyBears();

        harness.setLibrary(player1, List.of(bears)); // Only 1 card, but X=5

        harness.setHand(player1, List.of(new GenesisWave()));
        harness.addMana(player1, ManaColor.GREEN, 8); // X=5

        harness.castAndResolveSorcery(player1, 0, 5);

        // Should reveal the 1 card and prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
