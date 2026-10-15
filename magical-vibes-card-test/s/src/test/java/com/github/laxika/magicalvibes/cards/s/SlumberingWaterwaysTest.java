package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AnimateLand;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlumberingWaterways.class, AnimateLand.class, Mossdog.class})
class SlumberingWaterwaysTest extends BaseCardTest {

    @Test
    void entersTheBattlefieldTapped() {
        harness.setHand(player1, List.of(new SlumberingWaterways()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Slumbering Waterways").isTapped()).isTrue();
    }

    @Test
    void tapsForGreenOrBlueMana() {
        Permanent greenWaterway = harness.addToBattlefieldAndReturn(player1, new SlumberingWaterways());
        Permanent blueWaterway = harness.addToBattlefieldAndReturn(player1, new SlumberingWaterways());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(greenWaterway.isTapped()).isTrue();
        assertThat(blueWaterway.isTapped()).isTrue();
    }

    @Test
    void animatedWaterwayCannotBeBlockedByGroundCreature() {
        Permanent waterway = harness.addToBattlefieldAndReturn(player1, new SlumberingWaterways());
        waterway.setSummoningSick(false);
        addCreatureReady(player2, new Mossdog());
        animateLand(waterway);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void animatedWaterwayCanTapForManaAfterAttackingWithVigilance() {
        Permanent waterway = harness.addToBattlefieldAndReturn(player1, new SlumberingWaterways());
        waterway.setSummoningSick(false);
        animateLand(waterway);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(waterway.isAttacking()).isTrue();
        assertThat(waterway.isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(waterway.isTapped()).isTrue();
        assertThat(waterway.isAttacking()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void animatedWaterwayTramplesOverFlyingBlocker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SlumberingWaterways());
        attacker.setSummoningSick(false);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SlumberingWaterways());
        animateLand(attacker);
        animateLand(blocker);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Slumbering Waterways");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    private void animateLand(Permanent land) {
        harness.setHand(player1, List.of(new AnimateLand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, land.getId());
    }
}
