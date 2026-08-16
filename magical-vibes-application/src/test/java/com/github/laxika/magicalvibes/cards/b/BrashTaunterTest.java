package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrashTaunterTest extends BaseCardTest {

    @Test
    @DisplayName("Damage dealt to Brash Taunter is dealt to an opponent")
    void reflectsDamageToOpponent() {
        addReadyTaunter(player2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID taunterId = harness.getPermanentId(player2, "Brash Taunter");
        harness.castInstant(player1, 0, taunterId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player2, "Brash Taunter");
    }

    @Test
    @DisplayName("The activated ability makes Brash Taunter fight another creature")
    void fightsAnotherCreature() {
        Permanent taunter = addReadyTaunter(player1);
        Permanent bears = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(taunter);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(taunter.getMarkedDamage()).isEqualTo(2);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability cannot target a non-creature")
    void cannotTargetLand() {
        addReadyTaunter(player1);
        Permanent land = new Permanent(new com.github.laxika.magicalvibes.cards.f.Forest());
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyTaunter(com.github.laxika.magicalvibes.model.Player owner) {
        return addReadyCreature(owner, new BrashTaunter());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player owner) {
        return addReadyCreature(owner, new GrizzlyBears());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player owner,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(permanent);
        return permanent;
    }
}
