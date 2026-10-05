package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.c.Conflagrate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LokiLaufeyson.class, Divination.class, LightningBolt.class, Conflagrate.class})
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

    @Test
    void copiesOnlyTheFirstQualifyingSpellAndCanKeepItsTarget() {
        addReadyLoki();
        activateCopyAbility();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.assertLife(player2, 14);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 11);
    }

    @Test
    void abovePowerSpellLeavesTheTriggerAvailableForALaterSpell() {
        addReadyLoki();
        activateCopyAbility();
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.assertLife(player2, 14);
    }

    @Test
    void powerUpReducesItsCostByLokisManaCostOnEntryTurn() {
        Permanent loki = harness.enterBattlefieldAndReturn(player1, new LokiLaufeyson());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(loki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void powerIsCheckedWhenTheSpellIsCastRatherThanWhenAbilityResolves() {
        addReadyLoki();
        activateCopyAbility();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

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
    void canChooseANewTargetForTheCopy() {
        addReadyLoki();
        activateCopyAbility();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    void opponentSpellDoesNotConsumeTheDelayedTrigger() {
        addReadyLoki();
        activateCopyAbility();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 17);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.assertLife(player2, 14);
    }

    @Test
    void countsEveryXSymbolWhenComparingSpellManaValueToPower() {
        addReadyLoki();
        activateCopyAbility();
        harness.setHand(player1, List.of(new Conflagrate()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorceryForX(player1, 0, 1, Map.of(player2.getId(), 1));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
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
