package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CattiBrieOfMithralHall.class, BoneSaw.class, Bonesplitter.class, GrizzlyBears.class})
class CattiBrieOfMithralHallTest extends BaseCardTest {

    @Test
    void gainsCountersForEachAttachedEquipmentWhenAttacking() {
        Permanent cattiBrie = addCreatureReady(player1, new CattiBrieOfMithralHall());
        Permanent boneSaw = addEquipmentReady(player1, new BoneSaw());
        Permanent bonesplitter = addEquipmentReady(player1, new Bonesplitter());
        boneSaw.setAttachedTo(cattiBrie.getId());
        bonesplitter.setAttachedTo(cattiBrie.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(cattiBrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void removesAllCountersAndDealsThatMuchDamageToAttackingOpponentCreature() {
        Permanent cattiBrie = addCreatureReady(player1, new CattiBrieOfMithralHall());
        cattiBrie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(cattiBrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void cannotTargetYourOwnAttackingCreature() {
        Permanent cattiBrie = addCreatureReady(player1, new CattiBrieOfMithralHall());
        cattiBrie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cattiBrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addEquipmentReady(Player player, Card card) {
        Permanent equipment = new Permanent(card);
        equipment.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(equipment);
        return equipment;
    }
}
