package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AssaultFormation;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WardedBattlements.class, GrizzlyBears.class, AssaultFormation.class})
class WardedBattlementsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control get +1/+0")
    void buffsOwnAttackers() {
        harness.addToBattlefield(player1, new WardedBattlements());
        Permanent attacker = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-attacking creatures you control are unaffected")
    void doesNotAffectNonAttackers() {
        harness.addToBattlefield(player1, new WardedBattlements());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's attacking creatures are unaffected")
    void doesNotAffectOpponentAttackers() {
        harness.addToBattlefield(player1, new WardedBattlements());
        Permanent attacker = addAttackingBears(player2);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The bonus is removed when Warded Battlements leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new WardedBattlements());
        Permanent attacker = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Warded Battlements"));

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    private Permanent addAttackingBears(Player controller) {
        Permanent creature = addCreatureReady(controller, new GrizzlyBears());
        creature.setAttacking(true);
        return creature;
    }

    @Test
    @DisplayName("Warded Battlements also boosts itself when allowed to attack")
    @CardUsed({WardedBattlements.class, AssaultFormation.class})
    void boostsItselfWhenAttackingDespiteDefender() {
        Permanent wall = addCreatureReady(player1, new WardedBattlements());
        harness.addToBattlefield(player1, new AssaultFormation());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThat(als.canAttack(gd, wall, player1.getId())).isFalse();
        harness.activateAbility(player1, 1, 0, null, wall.getId());
        harness.passBothPriorities();
        assertThat(als.canAttack(gd, wall, player1.getId())).isTrue();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(wall.isAttacking()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(3);
    }

    @Test
    @DisplayName("The bonus ends as soon as a creature stops attacking")
    void bonusRemovedWhenCreatureStopsAttacking() {
        harness.addToBattlefield(player1, new WardedBattlements());
        Permanent attacker = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        attacker.setAttacking(false);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Warded Battlements bonuses stack")
    void multipleSourcesStack() {
        harness.addToBattlefield(player1, new WardedBattlements());
        harness.addToBattlefield(player1, new WardedBattlements());
        Permanent attacker = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }
}
