package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrystalBallTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Crystal Ball has {1}, {T}: Scry 2 activated ability")
    void hasCorrectProperties() {
        CrystalBall card = new CrystalBall();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}");
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(ScryEffect.class);
        ScryEffect effect = (ScryEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(effect.count()).isEqualTo(2);
    }

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addReadyCrystalBall(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Crystal Ball");
    }

    @Test
    @DisplayName("Activating ability taps Crystal Ball")
    void activatingTapsCrystalBall() {
        Permanent crystalBall = addReadyCrystalBall(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crystalBall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addReadyCrystalBall(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Scry resolution =====

    @Test
    @DisplayName("Resolving ability enters scry state with 2 cards")
    void resolvingEntersScryState() {
        addReadyCrystalBall(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);
        assertThat(gd.interaction.scryContext()).isNotNull();
        assertThat(gd.interaction.scryContext().cards()).hasSize(2);
    }

    @Test
    @DisplayName("Scry keeping both cards on top preserves them")
    void scryBothOnTop() {
        addReadyCrystalBall(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.getGameService().handleScryCompleted(gd, player1, List.of(0, 1), List.of());

        assertThat(deck.get(0)).isSameAs(top0);
        assertThat(deck.get(1)).isSameAs(top1);
    }

    @Test
    @DisplayName("Scry putting both cards on bottom moves them")
    void scryBothOnBottom() {
        addReadyCrystalBall(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.getGameService().handleScryCompleted(gd, player1, List.of(), List.of(0, 1));

        assertThat(deck.get(0)).isNotSameAs(top0);
        int deckSize = deck.size();
        assertThat(deck.get(deckSize - 2)).isSameAs(top0);
        assertThat(deck.get(deckSize - 1)).isSameAs(top1);
    }

    @Test
    @DisplayName("Scry putting one on top and one on bottom splits correctly")
    void scrySplitTopAndBottom() {
        addReadyCrystalBall(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Keep card 1 on top, put card 0 on bottom
        harness.getGameService().handleScryCompleted(gd, player1, List.of(1), List.of(0));

        assertThat(deck.get(0)).isSameAs(top1);
        assertThat(deck.get(deck.size() - 1)).isSameAs(top0);
    }

    @Test
    @DisplayName("Completing scry clears awaiting state")
    void scryCompletionClearsState() {
        addReadyCrystalBall(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleScryCompleted(gd, player1, List.of(0, 1), List.of());

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.interaction.scryContext()).isNull();
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Library with 1 card scries only that card")
    void libraryWithOneCard() {
        addReadyCrystalBall(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        Card singleCard = new GrizzlyBears();
        deck.add(singleCard);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);
        assertThat(gd.interaction.scryContext().cards()).hasSize(1);
    }

    @Test
    @DisplayName("Empty library scry does nothing")
    void emptyLibraryScry() {
        addReadyCrystalBall(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyCrystalBall(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent crystalBall = addReadyCrystalBall(player1);
        crystalBall.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== No summoning sickness for artifacts =====

    @Test
    @DisplayName("Can activate ability the turn it enters (no summoning sickness for artifacts)")
    void noSummoningSicknessForArtifact() {
        CrystalBall card = new CrystalBall();
        Permanent crystalBall = new Permanent(card);
        crystalBall.setSummoningSick(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(crystalBall);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crystalBall.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyCrystalBall(Player player) {
        CrystalBall card = new CrystalBall();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
