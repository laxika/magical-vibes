package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HuntedDragon;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({KilnmouthDragon.class, HuntedDragon.class, GrizzlyBears.class, SerraAngel.class})
class KilnmouthDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters for each Dragon card in your hand")
    void entersWithThreeCountersPerDragonInHand() {
        KilnmouthDragon card = new KilnmouthDragon();
        harness.setHand(player1, List.of(card, new HuntedDragon(), new HuntedDragon(), new GrizzlyBears()));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent dragon = findPermanentForCard(card);
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Deals damage equal to its +1/+1 counters to any target player")
    void dealsCounterDamageToPlayer() {
        Permanent dragon = addReadyDragon(player1);
        dragon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(dragon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals damage equal to its +1/+1 counters to any target creature")
    void dealsCounterDamageToCreature() {
        Permanent dragon = addReadyDragon(player1);
        dragon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent target = addReadyCreature(player2, new SerraAngel());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    private void addManaToCast() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private Permanent addReadyDragon(Player player) {
        Permanent dragon = new Permanent(new KilnmouthDragon());
        dragon.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(dragon);
        return dragon;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent findPermanentForCard(KilnmouthDragon card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
