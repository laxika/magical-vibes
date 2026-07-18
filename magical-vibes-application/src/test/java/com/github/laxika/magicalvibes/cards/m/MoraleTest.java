package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoraleTest extends BaseCardTest {

    @Test
    @DisplayName("Morale boosts attacking creatures with +1/+1")
    void boostsAttackingCreatures() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent nonAttacker = addReadyCreature(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Morale()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        // Attacking creature gets +1/+1
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(3);

        // Non-attacking creature is unaffected
        assertThat(nonAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(nonAttacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Morale effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.setHand(player1, List.of(new Morale()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
