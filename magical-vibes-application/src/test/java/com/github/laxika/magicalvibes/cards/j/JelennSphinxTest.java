package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JelennSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking pumps other attacking creatures but not itself")
    void pumpsOtherAttackersOnly() {
        Permanent sphinx = addReadyCreature(player1, new JelennSphinx());
        Permanent otherAttacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent homeBody = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(otherAttacker.getEffectivePower()).isEqualTo(3);
        assertThat(otherAttacker.getEffectiveToughness()).isEqualTo(3);
        assertThat(sphinx.getEffectivePower()).isEqualTo(1);
        assertThat(sphinx.getEffectiveToughness()).isEqualTo(5);
        assertThat(homeBody.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addReadyCreature(player1, new JelennSphinx());
        Permanent otherAttacker = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(otherAttacker.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(otherAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(otherAttacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking alone boosts nothing")
    void attackingAloneBoostsNothing() {
        Permanent sphinx = addReadyCreature(player1, new JelennSphinx());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(sphinx.getEffectivePower()).isEqualTo(1);
        assertThat(sphinx.getEffectiveToughness()).isEqualTo(5);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
