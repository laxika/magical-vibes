package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrambleFamiliar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SentinelOfLostLore.class, BrambleFamiliar.class})
class SentinelOfLostLoreTest extends BaseCardTest {

    private static final String RETURN_TO_HAND =
            "Return target card you own in exile that has an Adventure to your hand.";
    private static final String PUT_ON_BOTTOM =
            "Put target card you don't own in exile that has an Adventure on the bottom of its owner's library.";
    private static final String EXILE_GRAVEYARD = "Exile target player's graveyard.";

    @Test
    void returnsAnAdventureCardYouOwnFromExile() {
        Card adventure = new BrambleFamiliar();
        harness.setExile(player1, List.of(adventure));

        castSentinel();
        chooseMode(RETURN_TO_HAND);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(adventure.getId());
        harness.handlePermanentChosen(player1, adventure.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(adventure);
        assertThat(gd.findExiledCard(adventure.getId())).isNull();
    }

    @Test
    void putsAnAdventureCardYouDoNotOwnOnItsOwnersLibraryBottom() {
        Card adventure = new BrambleFamiliar();
        Card existingBottom = new SentinelOfLostLore();
        harness.setExile(player2, List.of(adventure));
        harness.setLibrary(player2, List.of(existingBottom));

        castSentinel();
        chooseMode(PUT_ON_BOTTOM);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(adventure.getId());
        harness.handlePermanentChosen(player1, adventure.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingBottom, adventure);
        assertThat(gd.findExiledCard(adventure.getId())).isNull();
    }

    @Test
    void exilesAPlayersGraveyard() {
        Card graveyardCard = new SentinelOfLostLore();
        harness.setGraveyard(player2, List.of(graveyardCard));

        castSentinel();
        chooseMode(EXILE_GRAVEYARD);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
    }

    @Test
    void choosingBothExileModesUsesSeparateTargets() {
        Card ownAdventure = new BrambleFamiliar();
        Card opposingAdventure = new BrambleFamiliar();
        harness.setExile(player1, List.of(ownAdventure));
        harness.setExile(player2, List.of(opposingAdventure));

        castSentinel();
        harness.handleListChoice(player1, RETURN_TO_HAND);
        harness.handleListChoice(player1, PUT_ON_BOTTOM);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);

        harness.handlePermanentChosen(player1, ownAdventure.getId());
        harness.handlePermanentChosen(player1, opposingAdventure.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownAdventure);
        assertThat(gd.playerDecks.get(player2.getId())).contains(opposingAdventure);
    }

    @Test
    void choosingAllThreeModesResolvesEachWithItsOwnTarget() {
        Card ownAdventure = new BrambleFamiliar();
        Card opposingAdventure = new BrambleFamiliar();
        Card existingBottom = new SentinelOfLostLore();
        Card graveyardCard = new SentinelOfLostLore();
        harness.setExile(player1, List.of(ownAdventure));
        harness.setExile(player2, List.of(opposingAdventure));
        harness.setLibrary(player2, List.of(existingBottom));
        harness.setGraveyard(player2, List.of(graveyardCard));

        castSentinel();
        harness.handleListChoice(player1, RETURN_TO_HAND);
        harness.handleListChoice(player1, PUT_ON_BOTTOM);
        harness.handleListChoice(player1, EXILE_GRAVEYARD);
        harness.handlePermanentChosen(player1, ownAdventure.getId());
        harness.handlePermanentChosen(player1, opposingAdventure.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownAdventure);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingBottom, opposingAdventure);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.findExiledCard(ownAdventure.getId())).isNull();
    }

    @Test
    void returnModeRejectsOpposingNonAdventureAndFaceDownCards() {
        Card ownAdventure = new BrambleFamiliar();
        Card opposingAdventure = new BrambleFamiliar();
        Card nonAdventure = new SentinelOfLostLore();
        Card faceDownAdventure = new BrambleFamiliar();
        harness.setExile(player1, List.of(ownAdventure, nonAdventure));
        harness.setExile(player2, List.of(opposingAdventure));
        gd.addToExile(player1.getId(), faceDownAdventure, null, true);

        castSentinel();
        chooseMode(RETURN_TO_HAND);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownAdventure.getId());
        harness.handlePermanentChosen(player1, ownAdventure.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownAdventure);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(nonAdventure, faceDownAdventure);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opposingAdventure);
    }

    @Test
    void bottomModeRejectsOwnNonAdventureAndFaceDownCards() {
        Card ownAdventure = new BrambleFamiliar();
        Card opposingAdventure = new BrambleFamiliar();
        Card nonAdventure = new SentinelOfLostLore();
        Card faceDownAdventure = new BrambleFamiliar();
        harness.setExile(player1, List.of(ownAdventure));
        harness.setExile(player2, List.of(opposingAdventure, nonAdventure));
        gd.addToExile(player2.getId(), faceDownAdventure, null, true);

        castSentinel();
        chooseMode(PUT_ON_BOTTOM);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(opposingAdventure.getId());
        harness.handlePermanentChosen(player1, opposingAdventure.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).contains(opposingAdventure);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownAdventure);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(nonAdventure, faceDownAdventure);
    }

    @Test
    void canExileYourOwnEntireGraveyard() {
        Card first = new BrambleFamiliar();
        Card second = new SentinelOfLostLore();
        Card opposingCard = new SentinelOfLostLore();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opposingCard));

        castSentinel();
        chooseMode(EXILE_GRAVEYARD);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCard);
    }

    @Test
    void mustChooseGraveyardModeWhenNoAdventureTargetsExistEvenWithEmptyGraveyards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        castSentinel();

        PendingInteraction.ColorChoice modes =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(modes.options()).containsExactly(EXILE_GRAVEYARD);
        chooseMode(EXILE_GRAVEYARD);
        PendingInteraction.PermanentChoice targets =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targets.validIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void remainingModeStillResolvesWhenAnExileTargetLeavesBeforeResolution() {
        Card ownAdventure = new BrambleFamiliar();
        Card opposingAdventure = new BrambleFamiliar();
        harness.setExile(player1, List.of(ownAdventure));
        harness.setExile(player2, List.of(opposingAdventure));

        castSentinel();
        harness.handleListChoice(player1, RETURN_TO_HAND);
        harness.handleListChoice(player1, PUT_ON_BOTTOM);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
        harness.handlePermanentChosen(player1, ownAdventure.getId());
        harness.handlePermanentChosen(player1, opposingAdventure.getId());
        gd.removeFromExile(ownAdventure.getId());
        harness.setGraveyard(player1, List.of(ownAdventure));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(ownAdventure);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownAdventure);
        assertThat(gd.playerDecks.get(player2.getId())).contains(opposingAdventure);
        assertThat(gd.findExiledCard(opposingAdventure.getId())).isNull();
    }

    private void chooseMode(String mode) {
        harness.handleListChoice(player1, mode);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
    }

    private void castSentinel() {
        harness.castFromHand(player1, new SentinelOfLostLore(), "{1}{G}{G}");
        harness.passBothPriorities();
    }
}
