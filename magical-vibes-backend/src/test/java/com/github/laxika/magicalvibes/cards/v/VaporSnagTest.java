package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VaporSnagTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Vapor Snag has correct card properties")
    void hasCorrectProperties() {
        VaporSnag card = new VaporSnag();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ReturnTargetPermanentToHandEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Vapor Snag puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Vapor Snag");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    // ===== Target validation =====

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // valid target so spell is playable
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // valid target so spell is playable
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving returns target creature to owner's hand and its controller loses 1 life")
    void resolvingReturnsCreatureAndControllerLosesLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Bouncing own creature causes self to lose 1 life")
    void bouncingOwnCreatureCausesSelfLifeLoss() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Caster is also the creature's controller, so they lose 1 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Vapor Snag goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Vapor Snag"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target is removed before resolution — no life loss")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // No life loss when spell fizzles
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }
}
