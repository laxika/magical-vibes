package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppealToEirduTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Single target gets +2/+1")
    void singleTargetGetsBoost() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AppealToEirdu()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two targets each get +2/+1")
    void twoTargetsEachGetBoost() {
        GrizzlyBears bear1Card = new GrizzlyBears();
        GrizzlyBears bear2Card = new GrizzlyBears();
        harness.addToBattlefield(player1, bear1Card);
        harness.addToBattlefield(player1, bear2Card);
        harness.setHand(player1, List.of(new AppealToEirdu()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castInstant(player1, 0, List.of(id1, id2));
        harness.passBothPriorities();

        bf = harness.getGameData().playerBattlefields.get(player1.getId());
        assertThat(bf.get(0).getPowerModifier()).isEqualTo(2);
        assertThat(bf.get(0).getToughnessModifier()).isEqualTo(1);
        assertThat(bf.get(1).getPowerModifier()).isEqualTo(2);
        assertThat(bf.get(1).getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cast with convoke — tap creatures to reduce mana cost")
    void castWithConvoke() {
        // Appeal to Eirdu costs {3}{W}. With 2 creatures tapped for convoke, need only 2 mana
        GrizzlyBears targetBear = new GrizzlyBears();
        GrizzlyBears convokeBear1 = new GrizzlyBears();
        GrizzlyBears convokeBear2 = new GrizzlyBears();
        harness.addToBattlefield(player1, targetBear);
        harness.addToBattlefield(player1, convokeBear1);
        harness.addToBattlefield(player1, convokeBear2);
        harness.setHand(player1, List.of(new AppealToEirdu()));
        harness.addMana(player1, ManaColor.WHITE, 2); // Only 2 mana, need convoke for the rest

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player1.getId());
        UUID targetId = bf.get(0).getId();
        UUID convokeId1 = bf.get(1).getId();
        UUID convokeId2 = bf.get(2).getId();

        harness.castInstantWithConvoke(player1, 0, List.of(targetId), List.of(convokeId1, convokeId2));

        // Convoke creatures should be tapped
        bf = harness.getGameData().playerBattlefields.get(player1.getId());
        assertThat(bf.get(1).isTapped()).isTrue();
        assertThat(bf.get(2).isTapped()).isTrue();

        // Resolve the spell
        harness.passBothPriorities();

        Permanent target = harness.getGameData().playerBattlefields.get(player1.getId()).get(0);
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cast with convoke using white creature to pay {W}")
    void castWithConvokeColoredPayment() {
        // Appeal costs {3}{W}. Tap a white creature (pays {W}), need only {3} from pool
        AngelicWall whiteCreature = new AngelicWall();
        GrizzlyBears targetBear = new GrizzlyBears();
        harness.addToBattlefield(player1, whiteCreature);
        harness.addToBattlefield(player1, targetBear);
        harness.setHand(player1, List.of(new AppealToEirdu()));
        // Add 3 generic mana (no white), but white creature will cover {W}
        harness.addMana(player1, ManaColor.RED, 3);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player1.getId());
        UUID whiteId = bf.get(0).getId();
        UUID targetId = bf.get(1).getId();

        harness.castInstantWithConvoke(player1, 0, List.of(targetId), List.of(whiteId));
        harness.passBothPriorities();

        // White creature should be tapped
        bf = harness.getGameData().playerBattlefields.get(player1.getId());
        assertThat(bf.get(0).isTapped()).isTrue();

        // Target should get boost
        assertThat(bf.get(1).getPowerModifier()).isEqualTo(2);
        assertThat(bf.get(1).getToughnessModifier()).isEqualTo(1);

        // Pool should have 0 mana left (3 red spent on {3})
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Spell fizzles when all targets removed before resolution")
    void fizzlesWhenAllTargetsRemoved() {
        GrizzlyBears bear1Card = new GrizzlyBears();
        GrizzlyBears bear2Card = new GrizzlyBears();
        harness.addToBattlefield(player1, bear1Card);
        harness.addToBattlefield(player1, bear2Card);
        harness.setHand(player1, List.of(new AppealToEirdu()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castInstant(player1, 0, List.of(id1, id2));

        // Remove both targets before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Spell should fizzle
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().gameLog).anyMatch(log -> log.contains("fizzles"));
        // Card should go to graveyard even when fizzled
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Appeal to Eirdu"));
    }

    @Test
    @DisplayName("Spell partially resolves when one of two targets removed")
    void partiallyResolvesWhenOneTargetRemoved() {
        GrizzlyBears bear1Card = new GrizzlyBears();
        GrizzlyBears bear2Card = new GrizzlyBears();
        harness.addToBattlefield(player1, bear1Card);
        harness.addToBattlefield(player1, bear2Card);
        harness.setHand(player1, List.of(new AppealToEirdu()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castInstant(player1, 0, List.of(id1, id2));

        // Remove only the first target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).removeFirst();

        harness.passBothPriorities();

        // Spell should partially resolve — second creature gets boost
        bf = harness.getGameData().playerBattlefields.get(player1.getId());
        assertThat(bf).hasSize(1);
        assertThat(bf.getFirst().getPowerModifier()).isEqualTo(2);
        assertThat(bf.getFirst().getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Card shows as playable with convoke when insufficient mana but enough creatures")
    void showsPlayableWithConvoke() {
        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        GrizzlyBears bear3 = new GrizzlyBears();
        harness.addToBattlefield(player1, bear1);
        harness.addToBattlefield(player1, bear2);
        harness.addToBattlefield(player1, bear3);
        harness.setHand(player1, List.of(new AppealToEirdu()));
        // Only 1 mana, but 3 untapped creatures = 4 total = enough for {3}{W}
        harness.addMana(player1, ManaColor.WHITE, 1);

        GameData gd = harness.getGameData();
        // The card should be playable (index 0)
        // We verify by attempting to cast with convoke
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        UUID targetId = bf.get(0).getId();
        UUID convokeId1 = bf.get(1).getId();
        UUID convokeId2 = bf.get(2).getId();

        // This should not throw - card is castable with convoke
        harness.castInstantWithConvoke(player1, 0, List.of(targetId), List.of(convokeId1, convokeId2));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Appeal to Eirdu");
    }
}

