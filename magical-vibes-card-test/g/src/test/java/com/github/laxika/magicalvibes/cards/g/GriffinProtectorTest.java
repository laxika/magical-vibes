package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DeadlyRecluse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GriffinProtector.class, DeadlyRecluse.class})
class GriffinProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when another creature you control enters")
    void getsBoostWhenAllyCreatureEnters() {
        harness.addToBattlefield(player1, new GriffinProtector());
        Permanent griffin = gd.playerBattlefields.get(player1.getId()).getFirst();

        castDeadlyRecluse(player1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void noBoostWhenOpponentCreatureEnters() {
        harness.addToBattlefield(player1, new GriffinProtector());
        Permanent griffin = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castDeadlyRecluse(player2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost is cumulative across multiple creature entries")
    void boostStacksForMultipleCreatures() {
        harness.addToBattlefield(player1, new GriffinProtector());
        Permanent griffin = gd.playerBattlefields.get(player1.getId()).getFirst();

        castDeadlyRecluse(player1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);

        castDeadlyRecluse(player1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GriffinProtector());
        Permanent griffin = gd.playerBattlefields.get(player1.getId()).getFirst();

        castDeadlyRecluse(player1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger for its own entry")
    void doesNotBoostItselfOnEntry() {
        harness.setHand(player1, List.of(new GriffinProtector()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent griffin = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, griffin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, griffin)).isEqualTo(3);
    }

    @Test
    @DisplayName("A second Griffin Protector boosts only the one already on the battlefield")
    void secondProtectorBoostsOnlyExistingProtector() {
        harness.addToBattlefield(player1, new GriffinProtector());
        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new GriffinProtector()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent second = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    private void castDeadlyRecluse(Player player) {
        harness.setHand(player, List.of(new DeadlyRecluse()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
    }
}
