package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArcaneAdaptation;
import com.github.laxika.magicalvibes.cards.s.SunSentinel;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForerunnerOfTheEmpire.class, RaptorCompanion.class, SunSentinel.class, ArcaneAdaptation.class})
class ForerunnerOfTheEmpireTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the search leaves the library unchanged")
    void decliningSearchLeavesLibraryUnchanged() {
        RaptorCompanion dinosaur = new RaptorCompanion();
        SunSentinel sentinel = new SunSentinel();
        harness.setLibrary(player1, List.of(sentinel, dinosaur));
        harness.setHand(player1, List.of(new ForerunnerOfTheEmpire()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sentinel, dinosaur);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching a library without Dinosaurs finishes without selecting a card")
    void searchWithoutDinosaursFinishes() {
        SunSentinel sentinel = new SunSentinel();
        harness.setLibrary(player1, List.of(sentinel));
        harness.setHand(player1, List.of(new ForerunnerOfTheEmpire()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sentinel);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opposing Dinosaur entering does not trigger damage")
    void opposingDinosaurDoesNotTrigger() {
        Permanent forerunner = harness.addToBattlefieldAndReturn(player2, new ForerunnerOfTheEmpire());
        harness.setHand(player1, List.of(new RaptorCompanion()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(forerunner.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Forerunner entering as a Dinosaur triggers both of its abilities")
    void enteringAsDinosaurTriggersItsOwnDamageAbility() {
        Permanent adaptation = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        adaptation.setChosenSubtype(CardSubtype.DINOSAUR);
        harness.setHand(player1, List.of(new ForerunnerOfTheEmpire()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("May search for a Dinosaur and put it on top of the library")
    void maySearchForDinosaurToTopOfLibrary() {
        harness.setHand(player1, List.of(new ForerunnerOfTheEmpire()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLibrary(player1, List.of(new RaptorCompanion(), new SunSentinel()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement()
                .satisfies(card -> assertThat(card.getSubtypes()).contains(CardSubtype.DINOSAUR));

        harness.handleCardChosen(player1, 0);

        assertThat(gameData.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(RaptorCompanion.class);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Dinosaur entering may deal 1 damage to each creature")
    void dinosaurEnteringMayDealDamageToEachCreature() {
        Permanent forerunner = harness.addToBattlefieldAndReturn(player1, new ForerunnerOfTheEmpire());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SunSentinel());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new SunSentinel());

        harness.setHand(player1, List.of(new RaptorCompanion()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(forerunner.getMarkedDamage()).isEqualTo(1);
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the Dinosaur trigger deals no damage")
    void decliningDinosaurTriggerDealsNoDamage() {
        Permanent forerunner = harness.addToBattlefieldAndReturn(player1, new ForerunnerOfTheEmpire());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SunSentinel());

        harness.setHand(player1, List.of(new RaptorCompanion()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(forerunner.getMarkedDamage()).isZero();
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A non-Dinosaur creature entering does not trigger the damage ability")
    void nonDinosaurEnteringDoesNotTrigger() {
        Permanent forerunner = harness.addToBattlefieldAndReturn(player1, new ForerunnerOfTheEmpire());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SunSentinel());

        harness.setHand(player1, List.of(new SunSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(forerunner.getMarkedDamage()).isZero();
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
