package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForerunnerOfTheEmpireTest extends BaseCardTest {

    @Test
    @DisplayName("May search for a Dinosaur and put it on top of the library")
    void maySearchForDinosaurToTopOfLibrary() {
        harness.setHand(player1, List.of(new ForerunnerOfTheEmpire()));
        harness.addMana(player1, ManaColor.RED, 4);
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(new RaptorCompanion(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement()
                .satisfies(card -> assertThat(card.getSubtypes()).contains(CardSubtype.DINOSAUR));

        harness.getGameService().handleInteractionAnswer(gameData, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gameData.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(RaptorCompanion.class);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Dinosaur entering may deal 1 damage to each creature")
    void dinosaurEnteringMayDealDamageToEachCreature() {
        Permanent forerunner = harness.addToBattlefieldAndReturn(player1, new ForerunnerOfTheEmpire());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(forerunner.getMarkedDamage()).isZero();
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
