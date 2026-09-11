package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hipparion;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Lure.class, GrizzlyBears.class, Mountain.class, Hipparion.class, AirElemental.class})
class LureTest extends BaseCardTest {

    @Test
    @DisplayName("All able creatures must block enchanted attacker")
    void allAbleCreaturesMustBlock() {
        Permanent enchantedAttacker = addCreatureReady(player1, new GrizzlyBears());
        enchantedAttacker.setAttacking(true);
        Permanent lure = harness.addToBattlefieldAndReturn(player1, new Lure());
        lure.setAttachedTo(enchantedAttacker.getId());

        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 2 blockers"));
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block by Lure")
    void tappedCreaturesNotForcedToBlock() {
        Permanent enchantedAttacker = addCreatureReady(player1, new GrizzlyBears());
        enchantedAttacker.setAttacking(true);
        Permanent lure = harness.addToBattlefieldAndReturn(player1, new Lure());
        lure.setAttachedTo(enchantedAttacker.getId());

        Permanent untapped = addCreatureReady(player2, new GrizzlyBears());
        Permanent tapped = addCreatureReady(player2, new GrizzlyBears());
        tapped.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Creatures unable to block enchanted attacker are not forced by Lure")
    void unableBlockersNotForced() {
        Permanent enchantedAttacker = addCreatureReady(player1, new AirElemental());
        enchantedAttacker.setAttacking(true);
        Permanent lure = harness.addToBattlefieldAndReturn(player1, new Lure());
        lure.setAttachedTo(enchantedAttacker.getId());
        Permanent nonFlying = addCreatureReady(player2, new GrizzlyBears());
        Permanent flying = addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(nonFlying.isBlocking()).isFalse();
        assertThat(flying.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Lure does not require a blocker to pay a blocking cost")
    void blockCostIsNotRequiredForLure() {
        Permanent enchantedAttacker = addCreatureReady(player1, new AirElemental());
        enchantedAttacker.setAttacking(true);
        Permanent lure = harness.addToBattlefieldAndReturn(player1, new Lure());
        lure.setAttachedTo(enchantedAttacker.getId());

        Permanent blocker = addCreatureReady(player2, new Hipparion());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Lure controlled by the defender still forces blocks against an opponent's creature")
    void lureForcesBlocksAgainstOpponentsCreature() {
        Permanent enchantedAttacker = addCreatureReady(player2, new GrizzlyBears());
        enchantedAttacker.setAttacking(true);

        Permanent firstBlocker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player1, new GrizzlyBears());
        Permanent lure = harness.addToBattlefieldAndReturn(player1, new Lure());
        lure.setAttachedTo(enchantedAttacker.getId());

        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");

        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Lure can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Lure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Lure").getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Lure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Casting Lure attaches it to a target creature")
    void castingLureAttachesToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Lure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && creature.getId().equals(permanent.getAttachedTo()));
    }
}
