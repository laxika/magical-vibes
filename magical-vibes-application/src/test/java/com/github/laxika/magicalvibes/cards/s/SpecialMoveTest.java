package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpecialMove.class, HillGiant.class, PithingNeedle.class})
class SpecialMoveTest extends BaseCardTest {

    @Test
    @DisplayName("Jump Kick destroys an artifact and Dash Attack adds two counters")
    void jumpKickAndDashAttack() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 1},
                List.of(artifact.getId(), attacker.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Pithing Needle");
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Foot Toss deals power damage before sacrificing its source")
    void footTossDamagesAndSacrificesSource() {
        Permanent dashTarget = new Permanent(new HillGiant());
        dashTarget.setSummoningSick(false);
        dashTarget.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(dashTarget);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{1, 2},
                List.of(dashTarget.getId(), player2.getId(), source.getId()));
        harness.passBothPriorities();

        assertThat(dashTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Dash Attack rejects a creature that is not attacking or blocking")
    void dashAttackRejectsInactiveCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 1},
                List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Foot Toss rejects the source creature as the other target")
    void footTossRequiresAnotherTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new SpecialMove()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 2, 2, new int[]{0, 2},
                List.of(artifact.getId(), source.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
