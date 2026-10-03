package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.Werebear;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenWindreader.class, Forest.class, Werebear.class})
class AvenWindreaderTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Aven Windreader puts it on the stack and resolves to battlefield")
    void castAndResolve() {
        harness.setHand(player1, List.of(new AvenWindreader()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Aven Windreader");
    }

    // ===== Activated ability: reveal top card =====

    @Test
    @DisplayName("Activating ability targeting opponent reveals top card of their library in game log")
    void revealOpponentTopCard() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Note the top card of player2's deck
        harness.setLibrary(player2, List.of(new Forest()));
        String topCardName = gd.playerDecks.get(player2.getId()).getFirst().getName();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("reveals") && log.contains(topCardName));
        // Card stays on top — deck size unchanged
        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(deckSizeBefore);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo(topCardName);
    }

    @Test
    @DisplayName("Activating ability targeting self reveals top card of own library in game log")
    void revealOwnTopCard() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setLibrary(player1, List.of(new Forest()));
        String topCardName = gd.playerDecks.get(player1.getId()).getFirst().getName();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("reveals") && log.contains(topCardName));
        assertThat(gd.playerDecks.get(player1.getId()).size()).isEqualTo(deckSizeBefore);
    }

    @Test
    @DisplayName("Generic activation cost can be paid with additional blue mana")
    void activateWithOnlyBlueMana() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains("Forest")).isTrue();
    }

    @Test
    @DisplayName("Targeting an opponent never names the controller's own top card")
    void revealReadsTheTargetLibraryOnly() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setLibrary(player1, List.of(new Werebear()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals") && log.contains("Forest"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("Werebear"));
    }

    @Test
    @DisplayName("Revealing top card when target player's library is empty logs appropriate message")
    void revealEmptyLibrary() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Ability can be activated multiple times per turn (no tap required)")
    void activateMultipleTimes() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLibrary(player2, List.of(new Forest()));

        // First activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        long revealCount = gd.gameLog.stream().map(GameLogEntry::plainText).filter(log -> log.contains("reveals")).count();
        assertThat(revealCount).isEqualTo(1);

        // Second activation — same permanent, not tapped
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        long revealCountAfter = gd.gameLog.stream().map(GameLogEntry::plainText).filter(log -> log.contains("reveals")).count();
        assertThat(revealCountAfter).isEqualTo(2);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Activating ability without a target throws exception")
    void activateWithoutTarget() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target player");
    }

    @Test
    @DisplayName("Activating ability targeting a permanent instead of a player throws exception")
    void activateTargetingPermanent() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addToBattlefield(player2, new Werebear());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID werebearId = harness.getPermanentId(player2, "Werebear");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, werebearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a player");
    }

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Aven Windreader")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new AvenWindreader());
        addCreatureReady(player2, new Werebear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Activating ability without enough mana throws exception")
    void activateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new AvenWindreader());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Windreader can activate its ability")
    void activateWhileTappedAndSummoningSick() {
        var windreader = harness.addToBattlefieldAndReturn(player1, new AvenWindreader());
        windreader.tap();
        windreader.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains("reveals Forest")).isTrue();
        assertThat(windreader.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability reveals the top card at resolution, not at activation")
    void revealUsesTopCardAtResolution() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addMana(player1, ManaColor.BLUE, 2);
        var originalTop = new Werebear();
        var nextCard = new Forest();
        harness.setLibrary(player2, List.of(originalTop, nextCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(gameLogContains("reveals")).isFalse();

        harness.setLibrary(player2, List.of(nextCard, originalTop));
        harness.passBothPriorities();

        assertThat(gameLogContains("reveals Forest")).isTrue();
        assertThat(gameLogContains("reveals Werebear")).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard, originalTop);
    }

    @Test
    @DisplayName("Generic mana alone cannot pay the blue activation cost")
    void activateWithoutBlueMana() {
        harness.addToBattlefield(player1, new AvenWindreader());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }
}

