package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TinderWallTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Tinder Wall adds two red mana")
    void sacrificeAddsTwoRedMana() {
        addCreatureReady(player1, new TinderWall());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Tinder Wall");
    }

    @Test
    @DisplayName("{R}, Sacrifice: deals 2 damage to the creature Tinder Wall is blocking")
    void damagesBlockedCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new TinderWall());

        blockWithWall();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Tinder Wall");
    }

    @Test
    @DisplayName("Damage ability cannot target a creature Tinder Wall isn't blocking")
    void cannotTargetUnblockedCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new TinderWall());
        otherAttacker.setAttacking(true);

        blockWithWall();
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, otherAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    /** Declares player1's first creature as an attacker and blocks it with player2's Tinder Wall. */
    private void blockWithWall() {
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
