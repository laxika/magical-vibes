package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.s.SealAway;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JoustingLance;
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

@CardUsed({BrokenBond.class, SealAway.class, Forest.class, JoustingLance.class, BalothGorger.class})
class BrokenBondTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Broken Bond puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new JoustingLance());
        harness.setHand(player1, List.of(new BrokenBond()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Jousting Lance");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Broken Bond");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target artifact and prompts may choice")
    void resolvesAndDestroysArtifactThenPromptsMay() {
        harness.addToBattlefield(player2, new JoustingLance());
        harness.setHand(player1, List.of(new BrokenBond(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Jousting Lance");
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        // Artifact should be destroyed
        harness.assertNotOnBattlefield(player2, "Jousting Lance");
        harness.assertInGraveyard(player2, "Jousting Lance");

        // Should prompt for may choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Resolving destroys target enchantment")
    void resolvesAndDestroysEnchantment() {
        harness.addToBattlefield(player2, new SealAway());
        harness.setHand(player1, List.of(new BrokenBond()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Seal Away");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Seal Away");
        harness.assertInGraveyard(player2, "Seal Away");
    }

    @Test
    @DisplayName("Accepting may choice and choosing a land puts it onto the battlefield")
    void acceptingMayPutsLandOntoBattlefield() {
        harness.addToBattlefield(player2, new JoustingLance());
        harness.setHand(player1, List.of(new BrokenBond(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Jousting Lance");
        harness.castAndResolveSorcery(player1, 0, targetId);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining may choice does not put land onto battlefield")
    void decliningMayLeavesHandUnchanged() {
        harness.addToBattlefield(player2, new JoustingLance());
        harness.setHand(player1, List.of(new BrokenBond(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Jousting Lance");
        harness.castAndResolveSorcery(player1, 0, targetId);
        harness.handleMayAbilityChosen(player1, false);

        // Artifact still destroyed
        harness.assertNotOnBattlefield(player2, "Jousting Lance");
        // Forest stays in hand
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new JoustingLance());
        harness.setHand(player1, List.of(new BrokenBond(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Jousting Lance");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Broken Bond");
    }

    @Test
    @DisplayName("Cannot target creature with Broken Bond")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new BalothGorger());
        harness.setHand(player1, List.of(new BrokenBond()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID creatureId = harness.getPermanentId(player2, "Baloth Gorger");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Putting a land does not consume the normal land play")
    void putLandDoesNotConsumeLandPlay() {
        harness.addToBattlefield(player1, new JoustingLance());
        harness.setHand(player1, List.of(new BrokenBond(), new Forest(), new Forest(), new BalothGorger()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Jousting Lance"));
        harness.handleMayAbilityChosen(player1, true);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 2))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Jousting Lance");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Baloth Gorger");
        harness.playLand(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A land can be put onto the battlefield after the normal land play")
    void putLandAfterLandPlay() {
        harness.addToBattlefield(player2, new JoustingLance());
        harness.setHand(player1, List.of(new Forest(), new BrokenBond(), new Forest()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Jousting Lance"));
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Broken Bond");
    }

    @Test
    @DisplayName("Accepting without any land in hand still destroys the artifact")
    void acceptingWithoutLandFinishesResolution() {
        harness.addToBattlefield(player2, new JoustingLance());
        harness.setHand(player1, List.of(new BrokenBond(), new BalothGorger()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Jousting Lance"));
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Jousting Lance");
        harness.assertInGraveyard(player1, "Broken Bond");
        harness.assertInHand(player1, "Baloth Gorger");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
