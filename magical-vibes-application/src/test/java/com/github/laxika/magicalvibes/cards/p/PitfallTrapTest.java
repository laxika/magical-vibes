package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PitfallTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys one attacking creature without flying")
    void destroysAttackingCreatureWithoutFlying() {
        Permanent attacker = addAttacker(new GrizzlyBears());
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pitfall Trap");
    }

    @Test
    @DisplayName("Can be cast for {W} when exactly one creature is attacking")
    void castsForAlternateCostWithExactlyOneAttacker() {
        Permanent attacker = addAttacker(new GrizzlyBears());
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithAlternateCost(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Alternate cost requires exactly one attacking creature")
    void alternateCostRequiresExactlyOneAttacker() {
        addAttacker(new GrizzlyBears());
        addAttacker(new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an attacking creature with flying")
    void cannotTargetAttackingCreatureWithFlying() {
        Permanent flyer = addAttacker(new CloudElemental());
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, flyer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
