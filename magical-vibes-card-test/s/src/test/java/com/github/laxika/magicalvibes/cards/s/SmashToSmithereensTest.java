package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelPlate;
import com.github.laxika.magicalvibes.cards.e.EnergyField;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.t.Tatterkite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmashToSmithereens.class, RodOfRuin.class, DarksteelPlate.class, GrizzlyBears.class,
        Tatterkite.class, EnergyField.class})
class SmashToSmithereensTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact and deals 3 damage to its controller")
    void destroysArtifactAndDealsDamage() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertLife(player2, lifeBefore - 3);
    }

    @Test
    @DisplayName("Destroys an artifact creature and deals 3 damage to its controller")
    void destroysArtifactCreature() {
        harness.addToBattlefield(player2, new Tatterkite());
        harness.setHand(player1, List.of(new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Tatterkite");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Tatterkite");
        harness.assertInGraveyard(player2, "Tatterkite");
        harness.assertLife(player2, lifeBefore - 3);
    }

    @Test
    @DisplayName("Damage rider remains controlled by the spell caster")
    void damageRiderUsesSpellControllerForDamageModifiers() {
        harness.addToBattlefield(player2, new EnergyField());
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Energy Field prevents damage from the player 1 spell because player 1 is its controller.
        harness.assertLife(player2, 20);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Deals damage to controller even when artifact is indestructible")
    void dealsDamageEvenWhenIndestructible() {
        harness.addToBattlefield(player2, new DarksteelPlate());
        harness.setHand(player1, List.of(new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Darksteel Plate");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Darksteel Plate is indestructible, still on battlefield
        harness.assertOnBattlefield(player2, "Darksteel Plate");
        harness.assertLife(player2, lifeBefore - 3);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertLife(player2, lifeBefore);
    }

    @Test
    @DisplayName("Can destroy your own artifact and deal damage to you")
    void destroysOwnArtifactAndDealsDamageToCaster() {
        harness.addToBattlefield(player1, new Tatterkite());
        harness.setHand(player1, List.of(new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Tatterkite");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Tatterkite");
        harness.assertInGraveyard(player1, "Tatterkite");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Energy Field prevents opponent spell damage when the artifact survives")
    void energyFieldPreventsDamageToIndestructibleArtifactController() {
        harness.addToBattlefield(player2, new EnergyField());
        harness.addToBattlefield(player2, new DarksteelPlate());
        harness.setHand(player1, List.of(new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Darksteel Plate");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Darksteel Plate");
        harness.assertOnBattlefield(player2, "Energy Field");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }
}
