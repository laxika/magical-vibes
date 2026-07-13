package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrcishOriflammeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control get +1/+0")
    void buffsOwnAttackingCreatures() {
        harness.addToBattlefield(player1, new OrcishOriflamme());
        Permanent bears = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff a non-attacking creature you control")
    void doesNotBuffNonAttackingCreatures() {
        harness.addToBattlefield(player1, new OrcishOriflamme());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findBears(player1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff an opponent's attacking creature")
    void doesNotBuffOpponentAttackers() {
        harness.addToBattlefield(player1, new OrcishOriflamme());
        Permanent bears = addAttackingBears(player2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus is removed when Orcish Oriflamme leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new OrcishOriflamme());
        Permanent bears = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Orcish Oriflamme"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    private Permanent addAttackingBears(Player controller) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        creature.setAttacking(true);
        gd.playerBattlefields.get(controller.getId()).add(creature);
        return creature;
    }

    private Permanent findBears(Player controller) {
        return gd.playerBattlefields.get(controller.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
    }
}
