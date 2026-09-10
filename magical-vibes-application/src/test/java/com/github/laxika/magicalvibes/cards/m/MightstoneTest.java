package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mightstone.class, GrizzlyBears.class})
class MightstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Mightstone boosts all attacking creatures")
    void boostsAllAttackingCreatures() {
        harness.addToBattlefield(player1, new Mightstone());
        Permanent ownAttacker = addAttackingBears(player1);
        Permanent opponentAttacker = addAttackingBears(player2);

        assertThat(gqs.getEffectivePower(gd, ownAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentAttacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mightstone does not boost non-attacking creatures")
    void doesNotBoostNonAttackingCreatures() {
        harness.addToBattlefield(player1, new Mightstone());
        Permanent ownCreature = addReadyBears(player1);
        Permanent opponentCreature = addReadyBears(player2);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    private Permanent addAttackingBears(Player controller) {
        Permanent creature = addReadyBears(controller);
        creature.setAttacking(true);
        return creature;
    }

    private Permanent addReadyBears(Player controller) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(controller.getId()).add(creature);
        return creature;
    }
}
