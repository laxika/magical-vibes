package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AssaultSuit;
import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MeliraTheLivingCure;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KethekCrucibleGoliath.class, AzureDrake.class, GrizzlyBears.class,
        FountainOfYouth.class, LlanowarElves.class, AssaultSuit.class, MeliraTheLivingCure.class})
class KethekCrucibleGoliathTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature reveals the first nonlegendary creature with lesser mana value")
    void sacrificesAndRevealsLowerManaValueCreature() {
        KethekCrucibleGoliath kethek = new KethekCrucibleGoliath();
        AzureDrake sacrificedCard = new AzureDrake();
        GrizzlyBears remainingCreature = new GrizzlyBears();
        AzureDrake equalManaValueCard = new AzureDrake();
        FountainOfYouth noncreatureCard = new FountainOfYouth();
        LlanowarElves foundCard = new LlanowarElves();

        harness.addToBattlefield(player1, kethek);
        harness.addToBattlefield(player1, sacrificedCard);
        harness.addToBattlefield(player1, remainingCreature);
        harness.setLibrary(player1, List.of(equalManaValueCard, noncreatureCard, foundCard));
        List<Card> library = gd.playerDecks.get(player1.getId());

        Permanent sacrificed = findPermanent(player1, "Azure Drake");

        moveToEndStep();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificedCard);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).contains(foundCard, remainingCreature);
        assertThat(library).containsExactlyInAnyOrder(equalManaValueCard, noncreatureCard);
    }

    @Test
    @DisplayName("Declining Kethek's trigger does not sacrifice a creature")
    void declineDoesNothing() {
        KethekCrucibleGoliath kethek = new KethekCrucibleGoliath();
        AzureDrake creature = new AzureDrake();

        harness.addToBattlefield(player1, kethek);
        harness.addToBattlefield(player1, creature);
        FountainOfYouth libraryCard = new FountainOfYouth();
        harness.setLibrary(player1, List.of(libraryCard));
        List<Card> library = gd.playerDecks.get(player1.getId());

        moveToEndStep();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).contains(creature);
        assertThat(library).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Accepting with no other creatures does nothing")
    void acceptWithNoOtherCreaturesDoesNothing() {
        KethekCrucibleGoliath kethek = new KethekCrucibleGoliath();
        LlanowarElves libraryCard = new LlanowarElves();

        harness.addToBattlefield(player1, kethek);
        harness.setLibrary(player1, List.of(libraryCard));
        List<Card> library = gd.playerDecks.get(player1.getId());

        moveToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).contains(kethek);
        assertThat(library).containsExactly(libraryCard);
    }


    @Test
    @DisplayName("A creature that cannot be sacrificed cannot pay for Kethek's reveal")
    void cannotSacrificeEquippedCreature() {
        harness.addToBattlefield(player1, new KethekCrucibleGoliath());
        AzureDrake creature = new AzureDrake();
        harness.addToBattlefield(player1, creature);
        harness.addToBattlefield(player1, new AssaultSuit());
        findPermanent(player1, "Assault Suit").setAttachedTo(findPermanent(player1, "Azure Drake").getId());
        LlanowarElves libraryCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(libraryCard));

        moveToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream().map(Permanent::getCard))
                .contains(creature).doesNotContain(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Legendary creatures are skipped and unrevealed cards remain above the revealed cards")
    void skipsLegendaryAndPreservesUnrevealedTop() {
        harness.addToBattlefield(player1, new KethekCrucibleGoliath());
        AzureDrake sacrificed = new AzureDrake();
        harness.addToBattlefield(player1, sacrificed);
        MeliraTheLivingCure legendary = new MeliraTheLivingCure();
        GrizzlyBears found = new GrizzlyBears();
        FountainOfYouth unrevealed = new FountainOfYouth();
        harness.setLibrary(player1, List.of(legendary, found, unrevealed));

        moveToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream().map(Permanent::getCard))
                .contains(found).doesNotContain(legendary);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, legendary);
    }

    @Test
    @DisplayName("No matching creature returns the entire revealed library")
    void noMatchingCreatureReturnsEntireLibrary() {
        harness.addToBattlefield(player1, new KethekCrucibleGoliath());
        LlanowarElves sacrificed = new LlanowarElves();
        harness.addToBattlefield(player1, sacrificed);
        GrizzlyBears tooExpensive = new GrizzlyBears();
        FountainOfYouth noncreature = new FountainOfYouth();
        harness.setLibrary(player1, List.of(tooExpensive, noncreature));

        moveToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(tooExpensive, noncreature);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream().map(Permanent::getCard))
                .doesNotContain(tooExpensive, sacrificed);
    }

    @Test
    @DisplayName("An empty library does not prevent sacrificing another creature")
    void emptyLibraryStillSacrifices() {
        harness.addToBattlefield(player1, new KethekCrucibleGoliath());
        GrizzlyBears sacrificed = new GrizzlyBears();
        harness.addToBattlefield(player1, sacrificed);
        harness.setLibrary(player1, List.of());

        moveToEndStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Kethek does not trigger at the opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new KethekCrucibleGoliath());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void moveToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
