package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.j.JhovallRider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Lunge.class, JhovallRider.class})
class LungeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to target creature and target player")
    void damagesCreatureAndPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new JhovallRider());
        harness.setHand(player1, List.of(new Lunge()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), player2.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @CardUsed(ChandraNalaar.class)
    @DisplayName("Deals 2 damage to a target planeswalker")
    void damagesCreatureAndPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new JhovallRider());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Lunge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), planeswalker.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target its controller as the player target")
    void damagesItsController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new JhovallRider());
        harness.setHand(player1, List.of(new Lunge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), player1.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Rejects a player as the creature target")
    void rejectsPlayerAsCreatureTarget() {
        harness.setHand(player1, List.of(new Lunge()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a creature as the player or planeswalker target")
    void rejectsCreatureAsPlayerOrPlaneswalkerTarget() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new JhovallRider());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new JhovallRider());
        harness.setHand(player1, List.of(new Lunge()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
