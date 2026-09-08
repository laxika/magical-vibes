package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StandardBearer.class, GrizzlyBears.class, ProdigalPyromancer.class, Shock.class})
class StandardBearerTest extends BaseCardTest {

    @Test
    void opponentMustTargetStandardBearerWhenAble() {
        Permanent standardBearer = addCreatureReady(player1, new StandardBearer());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");

        harness.castInstant(player2, 0, standardBearer.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void controllerIsNotForcedToTargetStandardBearer() {
        Permanent standardBearer = addCreatureReady(player1, new StandardBearer());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, otherCreature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(otherCreature.getId());
        assertThat(standardBearer.getId()).isNotEqualTo(otherCreature.getId());
    }

    @Test
    void opponentMustTargetStandardBearerWithActivatedAbilityWhenAble() {
        Permanent standardBearer = addCreatureReady(player1, new StandardBearer());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        int pyromancerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer);

        assertThatThrownBy(() -> harness.activateAbility(player2, pyromancerIndex, null, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");

        harness.activateAbility(player2, pyromancerIndex, null, standardBearer.getId());
        assertThat(gd.stack).hasSize(1);
    }
}
