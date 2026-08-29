package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EternalWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeastmastersMagemark.class, EternalWarrior.class, GrizzlyBears.class})
class BeastmastersMagemarkTest extends BaseCardTest {

    @Test
    @DisplayName("Gives +1/+1 to each enchanted creature you control")
    void boostsEachEnchantedCreatureYouControl() {
        Permanent beastmasterTarget = addReadyCreature(player1);
        Permanent otherEnchanted = addReadyCreature(player1);
        Permanent unenchanted = addReadyCreature(player1);

        attach(new BeastmastersMagemark(), beastmasterTarget, player1);
        attach(new EternalWarrior(), otherEnchanted, player1);

        assertThat(gqs.getEffectivePower(gd, beastmasterTarget)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beastmasterTarget)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherEnchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherEnchanted)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, unenchanted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unenchanted)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gives the enchanted blocked creature +1/+1 for each blocker")
    void boostsEnchantedCreatureForEachBlocker() {
        Permanent attacker = addReadyCreature(player1);
        attach(new BeastmastersMagemark(), attacker, player1);
        addReadyCreature(player2);
        addReadyCreature(player2);
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not grant the becomes-blocked bonus to an unenchanted creature")
    void doesNotBoostUnenchantedCreatureWhenBlocked() {
        Permanent enchanted = addReadyCreature(player1);
        attach(new BeastmastersMagemark(), enchanted, player1);
        Permanent unenchantedAttacker = addReadyCreature(player1);
        unenchantedAttacker.setAttacking(true);
        addReadyCreature(player2);

        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(unenchantedAttacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex)));

        assertThat(gd.stack).isEmpty();
        assertThat(unenchantedAttacker.getPowerModifier()).isZero();
        assertThat(unenchantedAttacker.getToughnessModifier()).isZero();
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void attach(Card auraCard, Permanent creature, Player controller) {
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }
}
