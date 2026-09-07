package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteerClear.class, GiantSpider.class, GrizzlyBears.class})
class SteerClearTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage without a Mount")
    void dealsTwoDamageWithoutMount() {
        Permanent target = addAttackingSpider();
        castSteerClear(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).contains(target.getId());
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 4 damage when a Mount was controlled as it was cast")
    void dealsFourDamageWhenMountWasControlledAsCast() {
        Permanent target = addAttackingSpider();
        GrizzlyBears mount = new GrizzlyBears();
        mount.setSubtypes(List.of(CardSubtype.MOUNT));
        harness.addToBattlefield(player2, mount);

        castSteerClear(target);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.setHand(player2, List.of(new SteerClear()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    private Permanent addAttackingSpider() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        target.setSummoningSick(false);
        target.setAttacking(true);
        return target;
    }

    private void castSteerClear(Permanent target) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SteerClear()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
    }
}
