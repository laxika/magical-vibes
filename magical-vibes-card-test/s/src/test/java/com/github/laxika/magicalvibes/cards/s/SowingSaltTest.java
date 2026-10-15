package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SowingSalt.class, TendoIceBridge.class, Plains.class, Shuko.class})
class SowingSaltTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target nonbasic land and every same-name copy from graveyard, hand, and library")
    void exilesTargetAndAllCopies() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new TendoIceBridge()).getId();
        harness.setHand(player2, List.of(new TendoIceBridge()));
        harness.setGraveyard(player2, List.of(new TendoIceBridge()));
        harness.setHand(player1, List.of(new SowingSalt(), new TendoIceBridge()));

        harness.setLibrary(player2, List.of(new TendoIceBridge(), new Plains()));

        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Tendo Ice Bridge");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Tendo Ice Bridge"))
                .hasSize(4);
        harness.assertNotInHand(player2, "Tendo Ice Bridge");
        harness.assertNotInGraveyard(player2, "Tendo Ice Bridge");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Tendo Ice Bridge"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
        harness.assertInHand(player1, "Tendo Ice Bridge");
    }

    @Test
    @DisplayName("Does not search if the target land leaves before resolution")
    void fizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new TendoIceBridge()).getId();
        harness.setHand(player2, List.of(new TendoIceBridge()));
        harness.setHand(player1, List.of(new SowingSalt()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Tendo Ice Bridge");
        harness.assertInGraveyard(player1, "Sowing Salt");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Tendo Ice Bridge"));
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Plains()).getId();
        harness.setHand(player1, List.of(new SowingSalt()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Shuko()).getId();
        harness.setHand(player1, List.of(new SowingSalt()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May leave matching hand and library cards while graveyard matches must be exiled")
    void mayDeclineHiddenZoneMatches() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new TendoIceBridge()).getId();
        TendoIceBridge handCopy = new TendoIceBridge();
        TendoIceBridge libraryCopy = new TendoIceBridge();
        TendoIceBridge graveyardCopy = new TendoIceBridge();
        harness.setHand(player2, List.of(handCopy));
        harness.setLibrary(player2, List.of(libraryCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setHand(player1, List.of(new SowingSalt()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player2, "Tendo Ice Bridge");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCopy);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCopy);
        harness.assertNotInGraveyard(player2, "Tendo Ice Bridge");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(graveyardCopy)
                .hasSize(2);
    }

    @Test
    @DisplayName("Can target its caster's land without exiling other battlefield copies")
    void canTargetOwnLandAndLeavesOtherBattlefieldCopies() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new TendoIceBridge()).getId();
        harness.addToBattlefield(player1, new TendoIceBridge());
        harness.addToBattlefield(player2, new TendoIceBridge());
        harness.setHand(player1, List.of(new SowingSalt()));
        harness.setHand(player2, List.of(new TendoIceBridge()));
        harness.setGraveyard(player1, List.of(new TendoIceBridge()));
        harness.setLibrary(player1, List.of(new Shuko()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetId));
        harness.assertOnBattlefield(player1, "Tendo Ice Bridge");
        harness.assertOnBattlefield(player2, "Tendo Ice Bridge");
        harness.assertInHand(player2, "Tendo Ice Bridge");
        harness.assertNotInGraveyard(player1, "Tendo Ice Bridge");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Shuko");
    }
}
