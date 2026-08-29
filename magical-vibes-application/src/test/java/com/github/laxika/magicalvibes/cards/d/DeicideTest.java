package com.github.laxika.magicalvibes.cards.d;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeicideTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an enchantment without exiling same-name cards when it is not a God")
    void nonGodOnlyExilesTarget() {
        harness.addToBattlefield(player2, new AuraOfSilence());
        Card handCopy = new AuraOfSilence();
        Card graveyardCopy = new AuraOfSilence();
        Card libraryCopy = new AuraOfSilence();
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy));

        castDeicide(harness.getPermanentId(player2, "Aura of Silence"));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Aura of Silence"))
                .hasSize(1);
        harness.assertInHand(player2, "Aura of Silence");
        harness.assertInGraveyard(player2, "Aura of Silence");
        assertThat(gd.playerDecks.get(player2.getId())).contains(libraryCopy);
    }

    @Test
    @DisplayName("A God enchantment offers any number of same-name cards from all three zones")
    void godOffersAnyNumberOfSameNameCards() {
        Permanent god = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());
        TestCards.mutableCard(god).setSubtypes(List.of(CardSubtype.GOD));
        Card handCopy = new AuraOfSilence();
        Card graveyardCopy = new AuraOfSilence();
        Card libraryCopy = new AuraOfSilence();
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy));

        castDeicide(god.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(handCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Aura of Silence"))
                .hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(graveyardCopy);
        assertThat(gd.playerDecks.get(player2.getId())).contains(libraryCopy);
    }

    @Test
    @DisplayName("Cannot target a nonenchantment permanent")
    void cannotTargetNonenchantment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Deicide()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    private void castDeicide(UUID targetId) {
        harness.setHand(player1, List.of(new Deicide()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
