package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ElectryteTest extends BaseCardTest {

    @Test
    @DisplayName("Deals its current power to each blocking creature when it deals combat damage to a player")
    void dealsCurrentPowerToBlockingCreatures() {
        harness.setLife(player2, 20);
        Permanent electryte = addCreatureReady(player1, new Electryte());
        electryte.setPowerModifier(2);
        addAttackingCreature(player1, creature("Harmless Attacker", 0, 5));
        Permanent blocker = addBlockingCreature(player2, creature("Blocker", 6, 6), 1);
        Permanent bystander = addCreatureReady(player2, creature("Bystander", 2, 6));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
        assertThat(bystander.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not trigger when Electryte is blocked")
    void doesNotTriggerWhenBlocked() {
        harness.setLife(player2, 20);
        Permanent electryte = addCreatureReady(player1, new Electryte());
        electryte.setPowerModifier(2);
        Permanent blocker = addBlockingCreature(player2, creature("Blocker", 6, 8), 0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent creature = addCreatureReady(player, card);
        creature.setAttacking(true);
        return creature;
    }

    private Permanent addBlockingCreature(Player player, Card card, int attackerIndex) {
        Permanent creature = addCreatureReady(player, card);
        creature.setBlocking(true);
        creature.addBlockingTarget(attackerIndex);
        return creature;
    }

    private Card creature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{W}");
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
