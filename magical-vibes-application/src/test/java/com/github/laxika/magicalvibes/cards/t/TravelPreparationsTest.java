package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TravelPreparations.class, WalkingCorpse.class})
class TravelPreparationsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting on one creature puts a +1/+1 counter on it")
    void singleTargetGetsCounter() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Walking Corpse");
        harness.castSorcery(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting on two creatures puts a +1/+1 counter on each")
    void twoTargetsEachGetCounter() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castSorcery(player1, 0, List.of(id1, id2));
        harness.passBothPriorities();

        bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf.get(0).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bf.get(1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castSorcery(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Walking Corpse");
        harness.castSorcery(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Travel Preparations");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback puts +1/+1 counter on target creature")
    void flashbackPutsCounter() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Walking Corpse");
        harness.castFlashback(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flashback on two targets puts +1/+1 counter on each")
    void flashbackTwoTargets() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.castFlashback(player1, 0, List.of(id1, id2));
        harness.passBothPriorities();

        bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf.get(0).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bf.get(1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flashback exiles the card after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Walking Corpse");
        harness.castFlashback(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Travel Preparations");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Travel Preparations"));
    }

    @Test
    @DisplayName("Flashback puts sorcery spell on stack")
    void flashbackPutsOnStackAsSorcery() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Walking Corpse");
        harness.castFlashback(player1, 0, List.of(bearId));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Travel Preparations");
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new TravelPreparations()));
        // Only 1 white mana, but flashback costs {1}{W}
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearId = harness.getPermanentId(player1, "Walking Corpse");
        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(bearId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can cast with zero targets on an empty battlefield")
    void zeroTargetsResolvesNormally() {
        harness.setHand(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Travel Preparations");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can flash back with zero targets even when a creature is available")
    void zeroTargetsFlashbackResolves() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castFlashback(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Travel Preparations");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Travel Preparations"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Remaining legal target receives its counter when another target leaves")
    void oneTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent removed = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent remaining = gd.playerBattlefields.get(player2.getId()).getFirst();

        harness.castSorcery(player1, 0, List.of(removed.getId(), remaining.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, removed);
        harness.passBothPriorities();

        assertThat(remaining.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Travel Preparations");
    }

    @Test
    @DisplayName("Flashback exiles the spell when all targets become illegal")
    void flashbackExilesWhenAllTargetsLeave() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.castFlashback(player1, 0, List.of(target.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Travel Preparations");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Travel Preparations"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void duplicateTargetsAreRejected() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        UUID target = harness.getPermanentId(player1, "Walking Corpse");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(target, target)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose more than two creatures")
    void threeTargetsAreRejected() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        List<UUID> targets = List.of(
                gd.playerBattlefields.get(player1.getId()).get(0).getId(),
                gd.playerBattlefields.get(player1.getId()).get(1).getId(),
                harness.getPermanentId(player2, "Walking Corpse"));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback requires white mana rather than the normal green cost")
    void flashbackRejectsNormalCastingMana() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new TravelPreparations()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        UUID target = harness.getPermanentId(player1, "Walking Corpse");

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(target)))
                .isInstanceOf(IllegalStateException.class);
    }
}
