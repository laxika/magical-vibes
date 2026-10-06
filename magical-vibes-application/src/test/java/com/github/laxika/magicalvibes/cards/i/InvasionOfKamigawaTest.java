package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RooftopSaboteurs;
import com.github.laxika.magicalvibes.cards.s.SkitteringSurveyor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Forest.class, GrizzlyBears.class, InvasionOfKamigawa.class, RooftopSaboteurs.class, SkitteringSurveyor.class})
class InvasionOfKamigawaTest extends BaseCardTest {

    @Test
    void entersTapsAndStunsTargetArtifactOrCreatureOpponentControls() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castInvasion(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void cannotTargetArtifactOrCreatureYouControl() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new InvasionOfKamigawa()));
        addBlueAndColorlessMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownCreature.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void defeatCastsRooftopSaboteursTransformed() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfKamigawa());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        Permanent saboteurs = findPermanent(player1, "Rooftop Saboteurs");
        assertThat(saboteurs.isTransformed()).isTrue();
    }

    @Test
    void rooftopSaboteursDrawsWhenItDealsCombatDamageToAPlayerOrBattle() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent saboteurs = addCreatureReady(player1, new RooftopSaboteurs());
        saboteurs.setAttacking(true);
        saboteurs.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void rooftopSaboteursAlsoDrawsWhenItDealsCombatDamageToABattle() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfKamigawa());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent saboteurs = addCreatureReady(player1, new RooftopSaboteurs());
        saboteurs.setAttacking(true);
        saboteurs.setAttackTarget(battle.getId());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void tapsAndStunsAnOpponentsArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkitteringSurveyor());

        castInvasion(target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void alreadyTappedCreatureStillGetsStunCounterAndSkipsOneUntap() {
        Permanent target = addCreatureReady(player2, new RooftopSaboteurs());
        target.tap();

        castInvasion(target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotTargetAnOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new InvasionOfKamigawa()));
        addBlueAndColorlessMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, land.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayDeclineCastingRooftopSaboteursAfterDefeat() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfKamigawa());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Invasion of Kamigawa");
        harness.assertNotOnBattlefield(player1, "Rooftop Saboteurs");
        assertThat(gd.findExiledCard(battle.getCard().getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castInvasion(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new InvasionOfKamigawa()));
        addBlueAndColorlessMana();
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }

    private void addBlueAndColorlessMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
