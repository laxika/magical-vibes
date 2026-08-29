package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NemesisTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles any attacking creature and creates its token copy for the caster")
    void exilesTargetAndCreatesTokenCopy() {
        Permanent whiteAttacker = attackingWhiteCreature();
        Permanent target = attackingCreature();
        castForAlternateCost(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(whiteAttacker);
    }

    @Test
    @DisplayName("The token copy is exiled at the beginning of the next end step")
    void tokenCopyExiledAtNextEndStep() {
        Permanent target = attackingWhiteCreature();
        castForAlternateCost(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The alternate cost requires a white creature to be attacking")
    void alternateCostRequiresWhiteAttacker() {
        Permanent target = attackingCreature();
        harness.setHand(player1, List.of(new NemesisTrap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void requiresAttackingTarget() {
        attackingWhiteCreature();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NemesisTrap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent attackingWhiteCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        creature.setAttacking(true);
        creature.setAttackTarget(player1.getId());
        return creature;
    }

    private Permanent attackingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setAttacking(true);
        creature.setAttackTarget(player1.getId());
        return creature;
    }

    private void castForAlternateCost(Permanent target) {
        harness.setHand(player1, List.of(new NemesisTrap()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();
    }
}
