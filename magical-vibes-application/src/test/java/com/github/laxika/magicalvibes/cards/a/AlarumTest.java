package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlarumTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps and boosts the target nonattacking creature")
    void untapsAndBoostsTarget() {
        Permanent target = addTappedCreature(player2);

        castAlarum(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent target = addTappedCreature(player2);
        castAlarum(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target an attacking creature")
    void cannotTargetAttacker() {
        addTappedCreature(player1); // a legal target must exist for the spell to be castable
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.setHand(player1, List.of(new Alarum()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonattacking");
    }

    private void castAlarum(Permanent target) {
        harness.setHand(player1, List.of(new Alarum()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addTappedCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.tap();
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
