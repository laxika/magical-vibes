package com.github.laxika.magicalvibes.cards.d;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.f.FontOfFertility;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.k.KruphixGodOfHorizons;
import com.github.laxika.magicalvibes.cards.p.PolymorphousRush;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Deicide.class, FontOfFertility.class, GoldenHind.class, KruphixGodOfHorizons.class,
        PolymorphousRush.class})
class DeicideTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an enchantment without exiling same-name cards when it is not a God")
    void nonGodOnlyExilesTarget() {
        harness.addToBattlefield(player2, new FontOfFertility());
        Card handCopy = new FontOfFertility();
        Card graveyardCopy = new FontOfFertility();
        Card libraryCopy = new FontOfFertility();
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy));

        castDeicide(harness.getPermanentId(player2, "Font of Fertility"));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Font of Fertility"))
                .hasSize(1);
        harness.assertInHand(player2, "Font of Fertility");
        harness.assertInGraveyard(player2, "Font of Fertility");
        assertThat(gd.playerDecks.get(player2.getId())).contains(libraryCopy);
    }

    @Test
    @DisplayName("A God enchantment offers any number of same-name cards from all three zones")
    void godOffersAnyNumberOfSameNameCards() {
        Permanent god = harness.addToBattlefieldAndReturn(player2, new KruphixGodOfHorizons());
        Card handCopy = new KruphixGodOfHorizons();
        Card graveyardCopy = new KruphixGodOfHorizons();
        Card libraryCopy = new KruphixGodOfHorizons();
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy));

        castDeicide(god.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(handCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Kruphix, God of Horizons"))
                .hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(graveyardCopy);
        assertThat(gd.playerDecks.get(player2.getId())).contains(libraryCopy);
    }

    @Test
    @DisplayName("Cannot target a nonenchantment permanent")
    void cannotTargetNonenchantment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        harness.setHand(player1, List.of(new Deicide()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    @Test
    void godCanExileAllMatchingCardsFromAllThreeZones() {
        Permanent god = harness.addToBattlefieldAndReturn(player2, new KruphixGodOfHorizons());
        Card handCopy = new KruphixGodOfHorizons();
        Card graveyardCopy = new KruphixGodOfHorizons();
        Card libraryCopy = new KruphixGodOfHorizons();
        Card unrelated = new GoldenHind();
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy, unrelated));

        castDeicide(god.getId());
        harness.handleMultipleCardsChosen(player1,
                List.of(handCopy.getId(), graveyardCopy.getId(), libraryCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(
                god.getOriginalCard(), handCopy, graveyardCopy, libraryCopy);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(unrelated);
    }

    @Test
    void godCanLeaveEveryMatchingCardInItsZone() {
        Permanent god = harness.addToBattlefieldAndReturn(player2, new KruphixGodOfHorizons());
        Card handCopy = new KruphixGodOfHorizons();
        Card graveyardCopy = new KruphixGodOfHorizons();
        Card libraryCopy = new KruphixGodOfHorizons();
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy));

        castDeicide(god.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(god.getOriginalCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCopy);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCopy);
    }

    @Test
    void nonGodCopyingAGodDoesNotSearchAfterExile() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new KruphixGodOfHorizons());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new FontOfFertility());
        }
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        Card matchingGod = new KruphixGodOfHorizons();
        harness.setLibrary(player1, List.of(matchingGod));
        harness.setHand(player1, List.of(new PolymorphousRush()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, source.getId());

        castDeicide(target.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getOriginalCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matchingGod);
    }

    private void castDeicide(UUID targetId) {
        harness.setHand(player1, List.of(new Deicide()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
