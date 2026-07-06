package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StormboundGeistTest extends BaseCardTest {

    

    @Test
    @DisplayName("Stormbound Geist can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent geist = new Permanent(new StormboundGeist());
        geist.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(geist);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(geist.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Stormbound Geist cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        Permanent geist = new Permanent(new StormboundGeist());
        geist.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(geist);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Undying returns Stormbound Geist with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StormboundGeist());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, geist.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returnedGeist = findPermanent(player1, "Stormbound Geist");
        assertThat(returnedGeist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Stormbound Geist"));
    }

    @Test
    @DisplayName("Undying does not return Stormbound Geist when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StormboundGeist());
        geist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, geist.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Stormbound Geist"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Stormbound Geist"));
        assertThat(gd.stack).isEmpty();
    }
}
