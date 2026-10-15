package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DualShot.class, QueensBaySoldier.class, JungleDelver.class, DiveDown.class, Forest.class})
class DualShotTest extends BaseCardTest {

    @Test
    @DisplayName("Resolves with zero targets on an empty battlefield")
    void zeroTargetsOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dual Shot");
        assertThat(gameLogContains("fizzles")).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Choosing zero targets leaves available creatures undamaged")
    void zeroTargetsWithCreatureAvailable() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        assertThat(findPermanent(player2, "Queen's Bay Soldier").getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Dual Shot");
        assertThat(gameLogContains("fizzles")).isFalse();
    }

    @Test
    @DisplayName("Can target a creature controlled by each player")
    void targetsEitherController() {
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(
                harness.getPermanentId(player1, "Queen's Bay Soldier"),
                harness.getPermanentId(player2, "Queen's Bay Soldier")));

        assertThat(findPermanent(player1, "Queen's Bay Soldier").getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanent(player2, "Queen's Bay Soldier").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target the same creature twice")
    void rejectsDuplicateTarget() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player2, "Queen's Bay Soldier");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(targetId, targetId)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Dual Shot");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose more than two creatures")
    void rejectsThreeTargets() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        List<UUID> targetIds = findPermanents(player2, "Queen's Bay Soldier").stream()
                .map(Permanent::getId).toList();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetIds))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Dual Shot");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a player")
    void rejectsPlayerTarget() {
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Dual Shot");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsLandTarget() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Dual Shot");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Skips a target that gains hexproof while damaging the remaining target")
    void skipsTargetThatGainsHexproof() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.addToBattlefield(player2, new QueensBaySoldier());
        List<Permanent> creatures = findPermanents(player2, "Queen's Bay Soldier");
        Permanent protectedCreature = creatures.get(0);
        Permanent otherCreature = creatures.get(1);
        harness.setHand(player1, List.of(new DualShot()));
        harness.setHand(player2, List.of(new DiveDown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, List.of(protectedCreature.getId(), otherCreature.getId()));
        harness.castInstant(player2, 0, protectedCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(otherCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Dual Shot");
        harness.assertInGraveyard(player2, "Dive Down");
    }

    @Test
    @DisplayName("Deals 1 damage to a single target creature")
    void singleTarget() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player2, "Queen's Bay Soldier");
        harness.castAndResolveInstant(player1, 0, List.of(bearId));

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to each of two target creatures")
    void twoTargets() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castAndResolveInstant(player1, 0, List.of(id1, id2));

        bf = harness.getGameData().playerBattlefields.get(player2.getId());
        assertThat(bf.get(0).getMarkedDamage()).isEqualTo(1);
        assertThat(bf.get(1).getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills 1/1 creatures")
    void killsOneOneCreatures() {
        harness.addToBattlefield(player2, new JungleDelver());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Jungle Delver");
        harness.castAndResolveInstant(player1, 0, List.of(targetId));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Jungle Delver");
    }

    @Test
    @DisplayName("Combines with marked damage to kill a creature")
    void killsPreviouslyDamagedCreature() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent creature = findPermanent(player2, "Queen's Bay Soldier");
        creature.setMarkedDamage(1);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Queen's Bay Soldier");
        harness.assertInGraveyard(player2, "Queen's Bay Soldier");
    }

    @Test
    @DisplayName("Partially resolves when one of two targets is removed")
    void partiallyResolvesWhenOneTargetRemoved() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castInstant(player1, 0, List.of(id1, id2));

        // Remove the first target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).removeFirst();

        harness.passBothPriorities();

        // Second creature should still take damage
        bf = harness.getGameData().playerBattlefields.get(player2.getId());
        assertThat(bf).hasSize(1);
        assertThat(bf.getFirst().getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Spell fizzles when all targets are removed before resolution")
    void fizzlesWhenAllTargetsRemoved() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castInstant(player1, 0, List.of(id1, id2));

        // Remove both targets before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Spell should fizzle
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        // Card should go to graveyard even when fizzled
        harness.assertInGraveyard(player1, "Dual Shot");
    }
}
