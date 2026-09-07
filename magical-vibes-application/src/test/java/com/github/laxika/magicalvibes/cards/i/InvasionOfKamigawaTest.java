package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RooftopSaboteurs;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({Forest.class, GrizzlyBears.class, InvasionOfKamigawa.class, RooftopSaboteurs.class})
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
        harness.passBothPriorities();

        Permanent saboteurs = findPermanent(player1, "Rooftop Saboteurs");
        assertThat(saboteurs.isTransformed()).isTrue();
        assertThat(saboteurs.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    void rooftopSaboteursDrawsWhenItDealsCombatDamageToAPlayerOrBattle() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        Permanent saboteurs = addCreatureReady(player1, new RooftopSaboteurs());
        saboteurs.setAttacking(true);
        saboteurs.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void rooftopSaboteursAlsoDrawsWhenItDealsCombatDamageToABattle() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfKamigawa());
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
