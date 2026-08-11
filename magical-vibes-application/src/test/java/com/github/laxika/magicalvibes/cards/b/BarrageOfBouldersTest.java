package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarrageOfBouldersTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature the caster does not control")
    void damagesOnlyOpponentsCreatures() {
        Permanent own = addCreatureReady(player1, makeCreature("Own Creature", 2, 2));
        Permanent opponent = addCreatureReady(player2, makeCreature("Opponent Creature", 2, 2));

        castBarrage();

        assertThat(own.getMarkedDamage()).isZero();
        assertThat(opponent.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not stop creatures from blocking without ferocious")
    void doesNotPreventBlockingWithoutFerocious() {
        Permanent attacker = addCreatureReady(player1, makeCreature("Attacker", 3, 3));
        Permanent blocker = addCreatureReady(player2, makeCreature("Blocker", 2, 2));

        castBarrage();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Stops all creatures from blocking when ferocious is active")
    void preventsBlockingWithFerocious() {
        Permanent attacker = addCreatureReady(player1, makeCreature("Attacker", 4, 4));
        addCreatureReady(player2, makeCreature("Blocker", 2, 2));

        castBarrage();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castBarrage() {
        harness.setHand(player1, List.of(new BarrageOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }

    private Card makeCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
