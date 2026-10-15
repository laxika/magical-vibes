package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MakeshiftMannequin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoardingDragon.class, DoomBlade.class, GrizzlyBears.class, Spellbook.class, MakeshiftMannequin.class})
class HoardingDragonTest extends BaseCardTest {


    @Test
    @DisplayName("ETB presents may prompt for library search")
    void etbPresentsMayPrompt() {
        setupDeck(List.of(new Spellbook(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new HoardingDragon()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting ETB may prompt presents only artifact cards from library")
    void acceptingEtbMayPresentsOnlyArtifacts() {
        Spellbook spellbook = new Spellbook();
        GrizzlyBears bears = new GrizzlyBears();
        setupDeck(List.of(spellbook, bears));
        harness.setHand(player1, List.of(new HoardingDragon()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        // Only artifact cards should be presented (Spellbook, not Grizzly Bears)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.ARTIFACT));
    }

    @Test
    @DisplayName("Choosing an artifact exiles it and imprints on Hoarding Dragon")
    void choosingArtifactExilesAndImprints() {
        Spellbook spellbook = new Spellbook();
        setupDeck(List.of(spellbook, new GrizzlyBears()));
        harness.setHand(player1, List.of(new HoardingDragon()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        // Choose the artifact (Spellbook)
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Spellbook should be in exile, face up (searches for a stated quality reveal the card)
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
        assertThat(gd.findExiledCard(spellbook.getId()).faceDown()).isFalse();

        // Hoarding Dragon should have Spellbook imprinted
        Permanent dragon = findPermanent(player1, "Hoarding Dragon");
        assertThat(gd.getImprintedCard(dragon.getCard())).isNotNull();
        assertThat(gd.getImprintedCard(dragon.getCard()).getName()).isEqualTo("Spellbook");
    }

    @Test
    @DisplayName("Declining ETB may prompt skips the library search")
    void decliningEtbMaySkipsSearch() {
        setupDeck(List.of(new Spellbook()));
        harness.setHand(player1, List.of(new HoardingDragon()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Hoarding Dragon");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }


    @Test
    @DisplayName("Death trigger returns imprinted card to owner's hand")
    void deathTriggerReturnsImprintedCardToHand() {
        HoardingDragon dragonCard = new HoardingDragon();
        harness.addToBattlefield(player1, dragonCard);

        // Manually imprint an artifact
        Spellbook spellbook = new Spellbook();
        Permanent dragon = findPermanent(player1, "Hoarding Dragon");
        gd.setImprintedCard(dragon.getCard(), spellbook);
        gd.addToExile(player1.getId(), spellbook);

        // Kill Hoarding Dragon with Doom Blade
        UUID dragonId = harness.getPermanentId(player1, "Hoarding Dragon");
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, dragonId);
        harness.passBothPriorities(); // resolve MayEffect from stack

        // Death trigger should present may prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        // Spellbook should be in player1's hand
        harness.assertInHand(player1, "Spellbook");

        // Spellbook should no longer be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Spellbook"));

        // Dragon should be in graveyard
        harness.assertInGraveyard(player1, "Hoarding Dragon");
    }

    @Test
    @DisplayName("Declining death trigger may prompt leaves the card in exile")
    void decliningDeathTriggerLeavesCardInExile() {
        HoardingDragon dragonCard = new HoardingDragon();
        harness.addToBattlefield(player1, dragonCard);

        // Manually imprint an artifact
        Spellbook spellbook = new Spellbook();
        Permanent dragon = findPermanent(player1, "Hoarding Dragon");
        gd.setImprintedCard(dragon.getCard(), spellbook);
        gd.addToExile(player1.getId(), spellbook);

        // Kill Hoarding Dragon with Doom Blade
        UUID dragonId = harness.getPermanentId(player1, "Hoarding Dragon");
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, dragonId);
        harness.passBothPriorities(); // resolve MayEffect from stack

        harness.handleMayAbilityChosen(player1, false);

        // Spellbook should remain in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));

        // Spellbook should NOT be in hand
        harness.assertNotInHand(player1, "Spellbook");
    }

    @Test
    @DisplayName("Death trigger does nothing if no card was imprinted")
    void deathTriggerDoesNothingWithNoImprint() {
        HoardingDragon dragonCard = new HoardingDragon();
        harness.addToBattlefield(player1, dragonCard);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Kill Hoarding Dragon with Doom Blade
        UUID dragonId = harness.getPermanentId(player1, "Hoarding Dragon");
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, dragonId);
        harness.passBothPriorities(); // resolve MayEffect from stack

        // May prompt should still fire
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        // Hand should be unchanged
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.assertInGraveyard(player1, "Hoarding Dragon");
    }

    @Test
    @DisplayName("Returning the Dragon before its death trigger resolves preserves the old hoard")
    void returningDragonBeforeDeathTriggerStillReturnsOldArtifact() {
        HoardingDragon dragon = new HoardingDragon();
        Spellbook artifact = new Spellbook();
        harness.setLibrary(player1, List.of(artifact));
        harness.setHand(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Hoarding Dragon"));
        harness.assertInGraveyard(player1, "Hoarding Dragon");

        harness.setHand(player1, List.of(new MakeshiftMannequin()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, dragon.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Hoarding Dragon");
        harness.assertInHand(player1, "Spellbook");
        assertThat(gd.findExiledCard(artifact.getId())).isNull();
    }

    @Test
    @DisplayName("An artifact can be left unfound even when the library contains one")
    void searchCanFailToFindArtifact() {
        Spellbook artifact = new Spellbook();
        harness.setLibrary(player1, List.of(artifact));
        harness.setHand(player1, List.of(new HoardingDragon()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An artifact found after the Dragon dies remains exiled")
    void dyingBeforeSearchResolvesLeavesArtifactExiled() {
        Spellbook artifact = new Spellbook();
        harness.setLibrary(player1, List.of(artifact));
        harness.setHand(player1, List.of(new HoardingDragon()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Hoarding Dragon"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertNotInHand(player1, "Spellbook");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Hoarding Dragon");
        harness.assertNotInHand(player1, "Spellbook");
        assertThat(gd.findExiledCard(artifact.getId())).isNotNull();
    }

    private void setupDeck(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

}
