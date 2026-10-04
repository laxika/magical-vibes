package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Extirpate.class, GrizzlyBears.class, Peek.class, Plains.class, ProdigalPyromancer.class, Shock.class})
class ExtirpateTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all same-name cards from the target card's owner's zones")
    void exilesAllSameNameCardsFromOwnersZones() {
        Card target = new GrizzlyBears();
        Card handCopy = new GrizzlyBears();
        Card libraryCopy = new GrizzlyBears();
        GameData gd = harness.getGameData();

        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));
        harness.setHand(player2, List.of(handCopy, new Peek()));
        harness.setLibrary(player2, List.of(libraryCopy, new Plains()));

        harness.setHand(player1, List.of(new Extirpate()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId(), handCopy.getId(), libraryCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(3);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Must exile all graveyard copies but may leave matching cards in hidden zones")
    void exilesAllPublicCopiesWhileAllowingHiddenCopiesToRemain() {
        Card target = new GrizzlyBears();
        Card graveyardCopy = new GrizzlyBears();
        Card handCopy = new GrizzlyBears();
        Card libraryCopy = new GrizzlyBears();

        harness.setGraveyard(player2, new ArrayList<>(List.of(target, graveyardCopy)));
        harness.setHand(player2, List.of(handCopy));
        harness.setLibrary(player2, List.of(libraryCopy));

        harness.setHand(player1, List.of(new Extirpate()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).contains(handCopy);
        assertThat(gd.playerDecks.get(player2.getId())).contains(libraryCopy);
    }

    @Test
    @DisplayName("Cannot target a basic land card in a graveyard")
    void cannotTargetBasicLand() {
        Card plains = new Plains();
        harness.setGraveyard(player2, new ArrayList<>(List.of(plains)));
        harness.setHand(player1, List.of(new Extirpate()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(plains);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Split second prevents a spell response")
    void splitSecondPreventsSpellResponse() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));
        harness.setHand(player1, List.of(new Extirpate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Searching your own zones still permits leaving hidden copies behind")
    void canTargetOwnGraveyardAndDeclineHiddenCopies() {
        Card target = new Extirpate();
        Card handCopy = new Extirpate();
        Card libraryCopy = new Extirpate();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Extirpate(), handCopy));
        harness.setLibrary(player1, List.of(libraryCopy));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCopy);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCopy);
        // The resolving spell was on the stack during the search.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1).doesNotContain(target);
    }

    @Test
    @DisplayName("An absent graveyard target prevents exiling its remaining copies")
    void doesNotSearchWhenTargetLeavesGraveyard() {
        Card target = new Extirpate();
        Card handCopy = new Extirpate();
        Card libraryCopy = new Extirpate();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player2, List.of(handCopy));
        harness.setLibrary(player2, List.of(libraryCopy));
        harness.setHand(player1, List.of(new Extirpate()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCopy);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCopy);
        harness.assertInGraveyard(player1, "Extirpate");
    }

    @Test
    @DisplayName("Split second permits activating a land's mana ability")
    void splitSecondAllowsManaAbility() {
        Card target = new Extirpate();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new Extirpate()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Split second prevents non-mana activated abilities")
    void splitSecondPreventsNonManaAbility() {
        Card target = new Extirpate();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player2, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new Extirpate()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("split second");
        assertThat(gd.stack).hasSize(1);
    }
}
