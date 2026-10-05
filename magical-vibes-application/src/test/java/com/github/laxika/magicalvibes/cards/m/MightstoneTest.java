package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mightstone.class, GrizzlyBears.class, MarchOfTheMachines.class})
class MightstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Mightstone boosts all attacking creatures")
    void boostsAllAttackingCreatures() {
        harness.addToBattlefield(player1, new Mightstone());
        Permanent ownAttacker = addAttackingBears(player1);
        Permanent opponentAttacker = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownAttacker)).isEqualTo(2);

        ownAttacker.setAttacking(false);
        opponentAttacker.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, opponentAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentAttacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mightstone does not boost non-attacking creatures")
    void doesNotBoostNonAttackingCreatures() {
        harness.addToBattlefield(player1, new Mightstone());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("An animated attacking Mightstone receives its own bonus")
    void animatedMightstoneBoostsItself() {
        Permanent mightstone = addCreatureReady(player1, new Mightstone());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        mightstone.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, mightstone)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mightstone)).isEqualTo(4);
    }

    @Test
    @DisplayName("Mightstone bonuses stack even when a Mightstone is tapped")
    void bonusesStackWhileTapped() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Mightstone());
        first.setTapped(true);
        harness.addToBattlefield(player2, new Mightstone());
        Permanent attacker = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mightstone's bonus ends when a creature stops attacking")
    void bonusEndsWhenAttackEnds() {
        harness.addToBattlefield(player1, new Mightstone());
        Permanent attacker = addAttackingBears(player1);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);

        attacker.setAttacking(false);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mightstone's bonus ends when Mightstone leaves the battlefield")
    void bonusEndsWhenSourceLeaves() {
        Permanent mightstone = harness.addToBattlefieldAndReturn(player1, new Mightstone());
        Permanent attacker = addAttackingBears(player2);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(mightstone);
        gd.playerGraveyards.get(player1.getId()).add(mightstone.getCard());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    private Permanent addAttackingBears(Player controller) {
        Permanent creature = addCreatureReady(controller, new GrizzlyBears());
        creature.setAttacking(true);
        return creature;
    }
}
