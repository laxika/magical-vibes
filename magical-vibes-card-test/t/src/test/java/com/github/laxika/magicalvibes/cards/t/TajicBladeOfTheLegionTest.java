package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArmoredWolfRider;
import com.github.laxika.magicalvibes.cards.b.BeetleformMage;
import com.github.laxika.magicalvibes.cards.f.FatalFumes;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TajicBladeOfTheLegion.class, BeetleformMage.class, ArmoredWolfRider.class, FatalFumes.class})
class TajicBladeOfTheLegionTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion gives Tajic +5/+5 when he attacks with two other creatures")
    void battalionBoostsTajic() {
        Permanent tajic = addCreatureReady(player1, new TajicBladeOfTheLegion());
        addCreatureReady(player1, new BeetleformMage());
        addCreatureReady(player1, new BeetleformMage());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(tajic.getPowerModifier()).isEqualTo(5);
        assertThat(tajic.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    @DisplayName("Battalion does not trigger with only one other attacker")
    void battalionDoesNotTriggerWithOneOtherAttacker() {
        Permanent tajic = addCreatureReady(player1, new TajicBladeOfTheLegion());
        addCreatureReady(player1, new BeetleformMage());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(tajic.getPowerModifier()).isEqualTo(0);
        assertThat(tajic.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent tajic = addCreatureReady(player1, new TajicBladeOfTheLegion());
        addCreatureReady(player1, new BeetleformMage());
        addCreatureReady(player1, new BeetleformMage());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(tajic.getPowerModifier()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tajic.getPowerModifier()).isEqualTo(0);
        assertThat(tajic.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Battalion does not trigger when Tajic stays out of combat")
    void battalionRequiresTajicToAttack() {
        Permanent tajic = addCreatureReady(player1, new TajicBladeOfTheLegion());
        addCreatureReady(player1, new BeetleformMage());
        addCreatureReady(player1, new BeetleformMage());
        addCreatureReady(player1, new BeetleformMage());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(tajic.getPowerModifier()).isZero();
        assertThat(tajic.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Removing another attacker in response does not stop battalion")
    void battalionStillResolvesAfterAnotherAttackerDies() {
        Permanent tajic = addCreatureReady(player1, new TajicBladeOfTheLegion());
        Permanent otherAttacker = addCreatureReady(player1, new BeetleformMage());
        addCreatureReady(player1, new BeetleformMage());
        harness.setHand(player2, List.of(new FatalFumes()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1, 2)));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player2, 0, otherAttacker.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherAttacker);
        resolveAllTriggers();

        assertThat(tajic.getPowerModifier()).isEqualTo(5);
        assertThat(tajic.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    @DisplayName("Tajic survives lethal combat damage without battalion")
    void indestructibleSurvivesLethalCombatDamage() {
        Permanent tajic = addCreatureReady(player1, new TajicBladeOfTheLegion());
        addCreatureReady(player2, new ArmoredWolfRider());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(tajic.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tajic);
    }

    @Test
    @DisplayName("Indestructible does not save Tajic from zero toughness")
    void zeroToughnessStillKillsTajic() {
        Permanent tajic = addCreatureReady(player1, new TajicBladeOfTheLegion());
        harness.setHand(player2, List.of(new FatalFumes()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0, tajic.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tajic);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tajic.getCard());
    }
}
