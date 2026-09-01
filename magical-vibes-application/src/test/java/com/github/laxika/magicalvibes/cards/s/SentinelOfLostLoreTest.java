package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrambleFamiliar;
import com.github.laxika.magicalvibes.cards.b.BonecrusherGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SentinelOfLostLore.class, BonecrusherGiant.class, BrambleFamiliar.class, LightningBolt.class})
class SentinelOfLostLoreTest extends BaseCardTest {

    private static final String RETURN_TO_HAND =
            "Return target card you own in exile that has an Adventure to your hand.";
    private static final String PUT_ON_BOTTOM =
            "Put target card you don't own in exile that has an Adventure on the bottom of its owner's library.";
    private static final String EXILE_GRAVEYARD = "Exile target player's graveyard.";

    @Test
    void returnsAnAdventureCardYouOwnFromExile() {
        Card adventure = new BonecrusherGiant();
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
        Card existingBottom = new LightningBolt();
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
        Card graveyardCard = new LightningBolt();
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
        Card ownAdventure = new BonecrusherGiant();
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

    private void chooseMode(String mode) {
        harness.handleListChoice(player1, mode);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
    }

    private void castSentinel() {
        harness.setHand(player1, List.of(new SentinelOfLostLore()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
