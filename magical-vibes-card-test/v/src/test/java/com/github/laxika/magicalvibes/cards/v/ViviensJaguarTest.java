package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.r.RummagingGoblin;
import com.github.laxika.magicalvibes.cards.t.TezzeretArtificeMaster;
import com.github.laxika.magicalvibes.cards.s.SnappingDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViviensJaguar.class, VivienReid.class, TezzeretArtificeMaster.class,
        Disperse.class, SnappingDrake.class, RummagingGoblin.class})
class ViviensJaguarTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard to its owner's hand while controlling Vivien")
    void returnsToHandWithVivienPlaneswalker() {
        ViviensJaguar jaguar = new ViviensJaguar();
        harness.setGraveyard(player1, List.of(jaguar));
        harness.addToBattlefield(player1, new VivienReid());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Vivien's Jaguar");
        harness.assertNotInGraveyard(player1, "Vivien's Jaguar");
    }

    @Test
    @DisplayName("Cannot activate without controlling a Vivien planeswalker")
    void cannotActivateWithoutVivienPlaneswalker() {
        harness.setGraveyard(player1, List.of(new ViviensJaguar()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control a Vivien planeswalker");
    }

    @Test
    @DisplayName("A non-Vivien planeswalker does not satisfy the activation condition")
    void cannotActivateWithOtherPlaneswalker() {
        harness.setGraveyard(player1, List.of(new ViviensJaguar()));
        harness.addToBattlefield(player1, new TezzeretArtificeMaster());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control a Vivien planeswalker");
    }

    @Test
    @DisplayName("An opponent's Vivien does not permit activation")
    void cannotActivateWithOpponentsVivien() {
        harness.setGraveyard(player1, List.of(new ViviensJaguar()));
        harness.addToBattlefield(player2, new VivienReid());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control a Vivien planeswalker");
    }

    @Test
    @DisplayName("Only the activated Jaguar returns, even with another copy in the graveyard")
    void returnsOnlyTheActivatedCopy() {
        ViviensJaguar first = new ViviensJaguar();
        ViviensJaguar second = new ViviensJaguar();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addToBattlefield(player1, new VivienReid());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("The ability still resolves after Vivien leaves the battlefield")
    void returnsAfterVivienLeaves() {
        harness.setGraveyard(player1, List.of(new ViviensJaguar()));
        var vivien = harness.addToBattlefieldAndReturn(player1, new VivienReid());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);

        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, vivien.getId());

        harness.assertNotOnBattlefield(player1, "Vivien Reid");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Vivien's Jaguar");
        harness.assertNotInGraveyard(player1, "Vivien's Jaguar");
    }

    @Test
    @DisplayName("The green mana requirement cannot be paid entirely with colorless mana")
    void cannotActivateWithoutGreenMana() {
        harness.setGraveyard(player1, List.of(new ViviensJaguar()));
        harness.addToBattlefield(player1, new VivienReid());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Vivien's Jaguar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reach allows the Jaguar to block a flying creature")
    void canBlockFlyingCreature() {
        var jaguar = harness.addToBattlefieldAndReturn(player1, new ViviensJaguar());
        var drake = harness.addToBattlefieldAndReturn(player2, new SnappingDrake());

        assertThat(bls.canBlockAttacker(gd, jaguar, drake,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("An older activation cannot return the Jaguar after it leaves and reenters the graveyard")
    @CardUsed({ViviensJaguar.class, VivienReid.class, RummagingGoblin.class, SnappingDrake.class})
    void olderActivationDoesNotReturnRediscardedJaguar() {
        addCreatureReady(player1, new RummagingGoblin());
        harness.addToBattlefield(player1, new VivienReid());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new ViviensJaguar()));
        harness.setLibrary(player1, List.of(new SnappingDrake()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Vivien's Jaguar");

        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Vivien's Jaguar");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Snapping Drake");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vivien's Jaguar");
        harness.assertNotInHand(player1, "Vivien's Jaguar");
    }
}
