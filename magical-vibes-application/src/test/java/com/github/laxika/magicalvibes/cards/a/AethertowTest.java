package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AethertowTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts target attacking creature on top of its owner's library")
    void resolvingPutsAttackerOnTopOfLibrary() {
        Permanent attacker = addAttacker(player2);
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        castAethertow(attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Resolving puts target blocking creature on top of its owner's library")
    void resolvingPutsBlockerOnTopOfLibrary() {
        Permanent blocker = addBlocker(player2);

        castAethertow(blocker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName())
                .isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Aethertow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the attacker leaves combat before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        Permanent attacker = addAttacker(player2);
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        castAethertow(attacker.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private void castAethertow(UUID targetId) {
        harness.setHand(player1, List.of(new Aethertow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, targetId);
    }

    private Permanent addAttacker(Player owner) {
        harness.addToBattlefield(owner, new GrizzlyBears());
        Permanent attacker = harness.getGameData().playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner) {
        harness.addToBattlefield(owner, new GrizzlyBears());
        Permanent blocker = harness.getGameData().playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }
}
