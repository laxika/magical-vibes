package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ApothecaryGeist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TakenosCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to an attacking Spirit")
    void damagesAttackingSpirit() {
        addCavalry(player1);
        Permanent spirit = addPermanent(player2, new ApothecaryGeist());
        spirit.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, spirit.getId());
        harness.passBothPriorities();

        assertThat(spirit.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to a blocking Spirit")
    void damagesBlockingSpirit() {
        addCavalry(player1);
        Permanent spirit = addPermanent(player2, new ApothecaryGeist());
        spirit.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, 0, spirit.getId());
        harness.passBothPriorities();

        assertThat(spirit.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a Spirit that is neither attacking nor blocking")
    void cannotTargetIdleSpirit() {
        addCavalry(player1);
        Permanent spirit = addPermanent(player2, new ApothecaryGeist());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, spirit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spirit");
    }

    @Test
    @DisplayName("Cannot target an attacking non-Spirit creature")
    void cannotTargetNonSpirit() {
        addCavalry(player1);
        Permanent bears = addPermanent(player2, new GrizzlyBears());
        bears.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spirit");
    }

    private void addCavalry(Player player) {
        addPermanent(player, new TakenosCavalry());
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
