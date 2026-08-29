package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaneswalkersScornTest extends BaseCardTest {

    @Test
    void debuffsTargetCreatureByRevealedManaValueUntilEndOfTurn() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersScorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower - 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness - 2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
    }

    @Test
    void emptyHandDoesNotDebuffCreature() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersScorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
    }

    @Test
    void requiresOpponentAndCreatureTargets() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersScorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player1.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
