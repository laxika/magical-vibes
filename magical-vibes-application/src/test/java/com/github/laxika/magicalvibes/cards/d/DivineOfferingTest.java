package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Blightwidow;
import com.github.laxika.magicalvibes.cards.b.BrassSquire;
import com.github.laxika.magicalvibes.cards.c.CopperCarapace;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivineOffering.class, CopperCarapace.class, DarksteelPlate.class, Blightwidow.class, BrassSquire.class})
class DivineOfferingTest extends BaseCardTest {



    @Test
    @DisplayName("Casting Divine Offering puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new CopperCarapace());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Copper Carapace");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Divine Offering destroys target artifact and gains life equal to its mana value")
    void destroysArtifactAndGainsLife() {
        harness.addToBattlefield(player2, new CopperCarapace());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        UUID targetId = harness.getPermanentId(player2, "Copper Carapace");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        // Copper Carapace should be destroyed
        harness.assertNotOnBattlefield(player2, "Copper Carapace");
        harness.assertInGraveyard(player2, "Copper Carapace");
        // Copper Carapace has mana value 1, so controller gains 1 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Divine Offering gains life even when target is indestructible")
    void gainsLifeEvenWhenIndestructible() {
        harness.addToBattlefield(player2, new DarksteelPlate());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        UUID targetId = harness.getPermanentId(player2, "Darksteel Plate");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        // Darksteel Plate is indestructible, should still be on battlefield
        harness.assertOnBattlefield(player2, "Darksteel Plate");
        // Darksteel Plate has mana value 3, controller still gains 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Cannot target a creature with Divine Offering")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new Blightwidow());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID creatureId = harness.getPermanentId(player2, "Blightwidow");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Divine Offering can target an artifact creature")
    void targetsArtifactCreature() {
        harness.addToBattlefield(player2, new BrassSquire());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        UUID targetId = harness.getPermanentId(player2, "Brass Squire");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Brass Squire");
        harness.assertInGraveyard(player2, "Brass Squire");
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Divine Offering fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new CopperCarapace());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        UUID targetId = harness.getPermanentId(player2, "Copper Carapace");
        harness.castInstant(player1, 0, targetId);
        // Remove the target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gameLogContains("fizzles")).isTrue();
        // No life gain when spell fizzles
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Divine Offering goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new CopperCarapace());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Copper Carapace");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Divine Offering");
    }
}
