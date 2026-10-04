package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireShrineKeeper.class, QueensBaySoldier.class, AncientBrontodon.class})
class FireShrineKeeperTest extends BaseCardTest {

    private void addReadyKeeper() {
        addCreatureReady(player1, new FireShrineKeeper());
    }

    @Test
    @DisplayName("Deals 3 damage to a single target creature")
    void singleTarget() {
        addReadyKeeper();
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        UUID bearId = harness.getPermanentId(player2, "Queen's Bay Soldier");
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bearId));
        harness.passBothPriorities();

        // 3 damage kills 2-toughness creature
        harness.assertNotOnBattlefield(player2, "Queen's Bay Soldier");
        harness.assertInGraveyard(player2, "Queen's Bay Soldier");
    }

    @Test
    @DisplayName("Deals 3 damage to each of two target creatures")
    void twoTargets() {
        addReadyKeeper();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new QueensBaySoldier());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new QueensBaySoldier());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        // Both 2-toughness creatures die to 3 damage each
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Queen's Bay Soldier");
    }

    @Test
    @DisplayName("Fire Shrine Keeper is sacrificed as cost when ability is activated")
    void sacrificedAsCost() {
        addReadyKeeper();
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        UUID bearId = harness.getPermanentId(player2, "Queen's Bay Soldier");
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bearId));

        // Sacrificed immediately as cost (before resolution)
        harness.assertNotOnBattlefield(player1, "Fire Shrine Keeper");
        harness.assertInGraveyard(player1, "Fire Shrine Keeper");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyKeeper();
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player2, "Queen's Bay Soldier");

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bearId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires tap: cannot activate if already tapped")
    void cannotActivateIfTapped() {
        addReadyKeeper();
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        // Pre-tap the keeper
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        UUID bearId = harness.getPermanentId(player2, "Queen's Bay Soldier");

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bearId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateWithNoTargets() {
        addReadyKeeper();
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());

        harness.assertInGraveyard(player1, "Fire Shrine Keeper");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void dealsExactlyThreeDamageToEachCreatureRegardlessOfController() {
        addReadyKeeper();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AncientBrontodon());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(own.getId(), opposing.getId()));
        harness.passBothPriorities();

        assertThat(own.getMarkedDamage()).isEqualTo(3);
        assertThat(opposing.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Ancient Brontodon");
        harness.assertOnBattlefield(player2, "Ancient Brontodon");
    }

    @Test
    void canTargetItselfAndStillDamageTheOtherTargetAfterSacrifice() {
        addReadyKeeper();
        UUID keeperId = harness.getPermanentId(player1, "Fire Shrine Keeper");
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(keeperId, target.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fire Shrine Keeper");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new FireShrineKeeper());
        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Fire Shrine Keeper");
    }

    @Test
    void cannotPayWithoutRedMana() {
        addReadyKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Fire Shrine Keeper");
    }

    @Test
    void cannotChooseTheSameCreatureTwice() {
        addReadyKeeper();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());
        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Fire Shrine Keeper");
    }

    @Test
    void cannotTargetAPlayer() {
        addReadyKeeper();
        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseMoreThanTwoCreatures() {
        addReadyKeeper();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new QueensBaySoldier());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new QueensBaySoldier());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new QueensBaySoldier());
        harness.addMana(player1, ManaColor.RED, 8);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Fire Shrine Keeper");
    }

    @Test
    void sevenManaIsNotEnough() {
        addReadyKeeper();
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Fire Shrine Keeper");
    }

    @Test
    void menaceRequiresAtLeastTwoBlockers() {
        addCreatureReady(player1, new FireShrineKeeper());
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.addToBattlefield(player2, new QueensBaySoldier());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }
}
