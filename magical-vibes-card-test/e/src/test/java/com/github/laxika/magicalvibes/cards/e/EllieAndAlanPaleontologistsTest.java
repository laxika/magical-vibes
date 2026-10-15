package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AltarsReap;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Ellie and Alan, Paleontologists")
@CardUsed({EllieAndAlanPaleontologists.class, Forest.class, GrizzlyBears.class, Ornithopter.class, AltarsReap.class})
class EllieAndAlanPaleontologistsTest extends BaseCardTest {

    @Test
    @DisplayName("Discover exiles skipped cards and the discovered card before the choice")
    void discoverCardsAreInExileDuringChoice() {
        addCreatureReady(player1, new EllieAndAlanPaleontologists());
        GrizzlyBears cost = new GrizzlyBears();
        Forest skipped = new Forest();
        GrizzlyBears discovered = new GrizzlyBears();
        Ornithopter remaining = new Ornithopter();
        harness.setGraveyard(player1, List.of(cost));
        harness.setLibrary(player1, List.of(skipped, discovered, remaining));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cost, skipped, discovered);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cost).doesNotContain(skipped, discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining, skipped);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can cast the discovered creature without paying mana")
    void castsDiscoveredCreatureForFree() {
        Permanent source = addCreatureReady(player1, new EllieAndAlanPaleontologists());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(source.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting a discovered spell requires payment of its additional sacrifice cost")
    void discoveredSpellRequiresAdditionalCostPayment() {
        addCreatureReady(player1, new EllieAndAlanPaleontologists());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new AltarsReap(), new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertOnBattlefield(player1, "Ellie and Alan, Paleontologists");
    }

    @Test
    @DisplayName("A noncreature graveyard card cannot pay the exile cost")
    void noncreatureCannotPayExileCost() {
        Permanent source = addCreatureReady(player1, new EllieAndAlanPaleontologists());
        harness.setGraveyard(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Discover zero skips lands and can cast a zero mana value creature")
    void discoverZeroCanCastZeroManaValueCreature() {
        addCreatureReady(player1, new EllieAndAlanPaleontologists());
        Forest skipped = new Forest();
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.setLibrary(player1, List.of(skipped, new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(skipped);
    }

    @Test
    @DisplayName("Cannot pay the tap cost while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new EllieAndAlanPaleontologists());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate during combat on its controller's turn")
    void cannotActivateDuringCombat() {
        Permanent source = addCreatureReady(player1, new EllieAndAlanPaleontologists());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Discovers up to the mana value of the exiled creature")
    void discoversUpToExiledCreatureManaValue() {
        addCreatureReady(player1, new EllieAndAlanPaleontologists());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), discovered));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Uses zero when the exiled creature has mana value zero")
    void usesZeroForZeroManaValueCreature() {
        addCreatureReady(player1, new EllieAndAlanPaleontologists());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.setLibrary(player1, List.of(discovered));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(discovered);
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureCard() {
        addCreatureReady(player1, new EllieAndAlanPaleontologists());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void canOnlyBeActivatedAtSorcerySpeed() {
        Permanent source = addCreatureReady(player1, new EllieAndAlanPaleontologists());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
