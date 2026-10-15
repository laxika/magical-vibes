package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeomancersGambit.class, Forest.class, Island.class, GrizzlyBears.class, Plains.class,
        DarksteelCitadel.class})
class GeomancersGambitTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target land, searches for an untapped basic, and draws a card")
    void destroysLandSearchesForBasicAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player2, List.of(new Island()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castGambit(target);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        chooseLibraryCard(player2, 0);

        assertThat(findPermanent(player2, "Island").isTapped()).isFalse();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The land controller may fail to find a basic and the caster still draws")
    void mayFailToFindAndCasterStillDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player2, List.of(new Island()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castGambit(target);

        harness.passBothPriorities();
        chooseLibraryCard(player2, -1);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GeomancersGambit()));
        addGambitMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    private void castGambit(Permanent target) {
        harness.setHand(player1, List.of(new GeomancersGambit()));
        addGambitMana();
        harness.castSorcery(player1, 0, target.getId());
    }

    private void addGambitMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void chooseLibraryCard(Player player, int index) {
        harness.getGameService().handleInteractionAnswer(
                gd, player, new InteractionAnswer.LibraryCardChosen(index));
    }


    @Test
    @DisplayName("Destroys a land, lets its controller search for an untapped basic land, and draws a card")
    void destroysLandSearchesAndDraws() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player2, List.of(new Plains()));
        harness.setHand(player1, List.of(new GeomancersGambit()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        harness.getGameService().handleInteractionAnswer(
                gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanent(player2, "Plains").isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can fail to find and still draws a card")
    void canFailToFindAndStillDraws() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new GeomancersGambit()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GeomancersGambit()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }


    @Test
    @DisplayName("The controller may decline searching entirely and the caster still draws")
    void mayDeclineSearchingEntirely() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        Island island = new Island();
        Plains plains = new Plains();
        harness.setLibrary(player2, List.of(island, plains));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castGambit(target);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(island, plains);
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertNotOnBattlefield(player2, "Plains");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target the caster's land and draws after their search")
    void canTargetOwnLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears()));
        castGambit(target);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().playerId()).isEqualTo(player1.getId());
        chooseLibraryCard(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(findPermanent(player1, "Island").isTapped()).isFalse();
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An indestructible land survives but its controller still searches and the caster draws")
    void indestructibleLandStillAllowsSearch() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        harness.setLibrary(player2, List.of(new Island()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castGambit(target);

        harness.passBothPriorities();
        chooseLibraryCard(player2, 0);

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertNotInGraveyard(player2, "Darksteel Citadel");
        assertThat(findPermanent(player2, "Island").isTapped()).isFalse();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A nonbasic land in the library cannot be found")
    void cannotFindNonbasicLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        DarksteelCitadel nonbasic = new DarksteelCitadel();
        harness.setLibrary(player2, List.of(nonbasic));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castGambit(target);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Darksteel Citadel");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nonbasic);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("If the target leaves before resolution, there is no search or draw")
    void missingTargetPreventsSearchAndDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player2, List.of(new Island()));
        GrizzlyBears draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));
        castGambit(target);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        harness.assertInGraveyard(player1, "Geomancer's Gambit");
    }
}
