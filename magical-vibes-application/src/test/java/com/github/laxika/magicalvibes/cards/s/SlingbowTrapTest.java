package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlingbowTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an attacking creature with flying")
    void destroysAttackingCreatureWithFlying() {
        Permanent attacker = addAttacker(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
        harness.assertInGraveyard(player1, "Slingbow Trap");
    }

    @Test
    @DisplayName("Can be cast for {G} when a black creature with flying is attacking")
    void castsForAlternateCostWhenBlackFlyingCreatureIsAttacking() {
        Permanent blackAttacker = addAttacker(player2, new VampireNighthawk());
        Permanent target = addAttacker(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blackAttacker);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Alternate cost requires a black attacking creature with flying")
    void alternateCostRequiresBlackFlyingAttacker() {
        Permanent attacker = addAttacker(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Cannot target a non-flying attacker")
    void cannotTargetNonFlyingAttacker() {
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player owner, Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
