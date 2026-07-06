package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RallyTheForcesTest extends BaseCardTest {

    

    @Test
    @DisplayName("Rally the Forces boosts attacking creatures with +1/+0 and first strike")
    void boostsAttackingCreatures() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent nonAttacker = addReadyCreature(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new RallyTheForces()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        // Attacking creature gets +1/+0 and first strike
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        // Non-attacking creature is unaffected
        assertThat(nonAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(nonAttacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(nonAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Rally the Forces effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.setHand(player1, List.of(new RallyTheForces()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
