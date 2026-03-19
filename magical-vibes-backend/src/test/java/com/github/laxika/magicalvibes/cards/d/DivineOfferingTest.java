package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndGainLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DivineOfferingTest extends BaseCardTest {

    @Test
    @DisplayName("Divine Offering has correct effect")
    void hasCorrectEffect() {
        DivineOffering card = new DivineOffering();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(DestroyTargetPermanentAndGainLifeEqualToManaValueEffect.class);
    }

    @Test
    @DisplayName("Casting Divine Offering puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Divine Offering");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Divine Offering destroys target artifact and gains life equal to its mana value")
    void destroysArtifactAndGainsLife() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Rod of Ruin should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Rod of Ruin"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Rod of Ruin"));
        // Rod of Ruin has mana value 4, so controller gains 4 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Divine Offering gains life even when target is indestructible")
    void gainsLifeEvenWhenIndestructible() {
        harness.addToBattlefield(player2, new DarksteelPlate());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        UUID targetId = harness.getPermanentId(player2, "Darksteel Plate");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Darksteel Plate is indestructible, should still be on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Darksteel Plate"));
        // Darksteel Plate has mana value 3, controller still gains 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Cannot target a creature with Divine Offering")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Divine Offering fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castInstant(player1, 0, targetId);
        // Remove the target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // No life gain when spell fizzles
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Divine Offering goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Divine Offering"));
    }
}
