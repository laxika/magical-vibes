package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandWithManaValueConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerilousVoyageTest extends BaseCardTest {

    // ===== Bounce + scry (MV ≤ 2) =====

    @Test
    @DisplayName("Bounces target and enters scry state when mana value is 2 or less")
    void bouncesAndScrysWhenManaValueAtMost2() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // MV 2
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Creature bounced
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Scry triggered
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Scry triggers for MV 1 creature")
    void scryTriggersForManaValue1() {
        harness.addToBattlefield(player2, new LlanowarElves()); // MV 1
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    @DisplayName("Scry completes and spell goes to graveyard")
    void scryCompletesAndSpellGoesToGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // MV 2
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Complete scry
        gs.handleScryCompleted(gd, player1, List.of(0, 1), List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Perilous Voyage"));
    }

    // ===== Bounce without scry (MV > 2) =====

    @Test
    @DisplayName("Bounces target but does NOT scry when mana value is greater than 2")
    void bouncesWithoutScryWhenManaValueAbove2() {
        harness.addToBattlefield(player2, new HillGiant()); // MV 4
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Creature bounced
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));

        // No scry — spell should fully resolve
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Perilous Voyage"));
    }

    // ===== Targeting restrictions =====

    @Test
    @DisplayName("Cannot target own permanent")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // valid target so spell is playable
        harness.addToBattlefield(player1, new LlanowarElves());
        UUID ownTargetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownTargetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent you don't control");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // valid target so spell is playable
        harness.addToBattlefield(player2, new Island());
        UUID landId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent you don't control");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target is removed before resolution — no bounce, no scry")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Perilous Voyage"));
    }
}
