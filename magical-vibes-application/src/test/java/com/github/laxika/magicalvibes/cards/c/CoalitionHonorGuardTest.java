package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelfireCrusader;
import com.github.laxika.magicalvibes.cards.d.DegaDisciple;
import com.github.laxika.magicalvibes.cards.j.Jilt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoalitionHonorGuard.class, AngelfireCrusader.class, DegaDisciple.class, Jilt.class})
class CoalitionHonorGuardTest extends BaseCardTest {

    @Test
    void opponentMustTargetHonorGuardWhenAble() {
        Permanent honorGuard = addCreatureReady(player1, new CoalitionHonorGuard());
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());

        harness.setHand(player2, List.of(new Jilt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");

        harness.castInstant(player2, 0, honorGuard.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(honorGuard.getId());
    }

    @Test
    void opponentMustTargetHonorGuardWithActivatedAbilityWhenAble() {
        Permanent honorGuard = addCreatureReady(player1, new CoalitionHonorGuard());
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());
        Permanent disciple = addCreatureReady(player2, new DegaDisciple());

        int discipleIndex = gd.playerBattlefields.get(player2.getId()).indexOf(disciple);
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, discipleIndex, null, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");

        harness.activateAbility(player2, discipleIndex, null, honorGuard.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(honorGuard.getId());
    }

    @Test
    void controllerIsNotForcedToTargetItsOwnHonorGuard() {
        Permanent honorGuard = addCreatureReady(player1, new CoalitionHonorGuard());
        Permanent otherCreature = addCreatureReady(player1, new AngelfireCrusader());

        harness.setHand(player1, List.of(new Jilt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, otherCreature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(otherCreature.getId());
        assertThat(honorGuard.getId()).isNotEqualTo(otherCreature.getId());
    }
}
