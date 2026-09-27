package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurfWar.class, Forest.class, GrizzlyBears.class})
class TurfWarTest extends BaseCardTest {

    @Test
    void entersWithAContestedLandForEachPlayer() {
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player2Land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent player2OtherLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, java.util.List.of(new TurfWar()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(player2Land.getId(), player2OtherLand.getId());
        harness.handleMultiplePermanentsChosen(player1, java.util.List.of(player2OtherLand.getId()));

        assertThat(player1Land.getCounterCount(CounterType.CONTESTED)).isEqualTo(1);
        assertThat(player2Land.getCounterCount(CounterType.CONTESTED)).isZero();
        assertThat(player2OtherLand.getCounterCount(CounterType.CONTESTED)).isEqualTo(1);
    }

    @Test
    void combatDamageToOpponentTransfersAndUntapsContestedLand() {
        harness.addToBattlefieldAndReturn(player1, new TurfWar());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.setCounterCount(CounterType.CONTESTED, 1);
        land.tap();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1, player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void combatDamageToControllerTransfersOwnContestedLandToAttacker() {
        harness.addToBattlefieldAndReturn(player1, new TurfWar());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.CONTESTED, 1);
        land.tap();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2, player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(land.isTapped()).isFalse();
    }

    private void resolveCombat(Player attacker, Player defender) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
