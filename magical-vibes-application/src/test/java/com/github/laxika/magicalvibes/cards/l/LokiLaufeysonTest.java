package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LokiLaufeyson.class, Divination.class, LightningBolt.class})
class LokiLaufeysonTest extends BaseCardTest {

    @Test
    void copiesTheNextInstantOrSorceryWithinLokiPower() {
        addReadyLoki();
        activateCopyAbility();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getDescription().contains("delayed trigger"));
    }

    @Test
    void doesNotConsumeForASpellAboveLokiPower() {
        addReadyLoki();
        activateCopyAbility();

        harness.setLibrary(player1, List.of(
                new LightningBolt(), new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void usesLokiCurrentPowerAfterPowerUp() {
        Permanent loki = addReadyLoki();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(loki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        activateCopyAbility();
        harness.setLibrary(player1, List.of(
                new LightningBolt(), new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void usesLokiLastKnownPowerAfterHeLeavesTheBattlefield() {
        Permanent loki = addReadyLoki();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        activateCopyAbility();
        loki.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.setLibrary(player1, List.of(
                new LightningBolt(), new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void powerUpCanBeActivatedOnlyOnce() {
        addReadyLoki();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyLoki() {
        return addCreatureReady(player1, new LokiLaufeyson());
    }

    private void activateCopyAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
