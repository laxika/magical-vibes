package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed({WellRested.class, WatchfulRadstag.class})
class WellRestedTest extends BaseCardTest {

    @Test
    @DisplayName("Well Rested can be cast targeting an opponent's creature")
    void canEnchantOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new WatchfulRadstag());
        harness.setHand(player1, List.of(new WellRested()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Well Rested").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
    }

    @Test
    @DisplayName("Untapping the enchanted creature puts two counters on it, gains life, and draws a card")
    void untappingEnchantedCreatureResolvesAllEffects() {
        Permanent creature = addCreatureReady(player1, new WatchfulRadstag());
        attachAura(player1, creature);
        creature.tap();
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new WatchfulRadstag()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.performUntapStep(player1);
        resolveAllTriggers();

        assertThat(creature.getCounters().get(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Well Rested triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        Permanent creature = addCreatureReady(player1, new WatchfulRadstag());
        attachAura(player1, creature);
        creature.tap();
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new WatchfulRadstag(), new WatchfulRadstag()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.performUntapStep(player1);
        resolveAllTriggers();

        creature.tap();
        harness.performUntapStep(player1);
        resolveAllTriggers();

        assertThat(creature.getCounters().get(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The opponent controlling the enchanted creature gains the life and draws the card")
    void triggersForOpponentControlledEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new WatchfulRadstag());
        attachAura(player1, creature);
        creature.tap();
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.setLibrary(player1, List.of(new WatchfulRadstag()));
        harness.setLibrary(player2, List.of(new WatchfulRadstag()));
        int auraControllerHandSize = gd.playerHands.get(player1.getId()).size();
        int creatureControllerHandSize = gd.playerHands.get(player2.getId()).size();

        harness.performUntapStep(player2);
        resolveAllTriggers();

        assertThat(creature.getCounters().get(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(auraControllerHandSize);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(creatureControllerHandSize + 1);
    }

    @Test
    @DisplayName("The enchanted creature is the source of the granted untap ability")
    void enchantedCreatureIsAbilitySource() {
        Permanent creature = addCreatureReady(player1, new WatchfulRadstag());
        attachAura(player1, creature);
        creature.tap();

        harness.performUntapStep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Moving the Aura after the trigger does not redirect its counters")
    void movingAuraDoesNotRedirectPendingCounters() {
        Permanent originalCreature = addCreatureReady(player1, new WatchfulRadstag());
        Permanent newCreature = addCreatureReady(player1, new WatchfulRadstag());
        Permanent aura = attachAura(player1, originalCreature);
        originalCreature.tap();
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new WatchfulRadstag()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.performUntapStep(player1);
        assertThat(gd.stack).hasSize(1);
        aura.setAttachedTo(newCreature.getId());
        resolveAllTriggers();

        assertThat(originalCreature.getCounters().get(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(newCreature.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Untapping another creature does not consume the enchanted creature's trigger")
    void unrelatedUntapDoesNotConsumeTrigger() {
        Permanent creature = addCreatureReady(player1, new WatchfulRadstag());
        Permanent unrelatedCreature = addCreatureReady(player1, new WatchfulRadstag());
        attachAura(player1, creature);
        unrelatedCreature.tap();
        harness.setLibrary(player1, List.of(new WatchfulRadstag()));

        harness.performUntapStep(player1);

        assertThat(gd.stack).isEmpty();
        creature.tap();
        harness.performUntapStep(player1);
        resolveAllTriggers();

        assertThat(creature.getCounters().get(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(unrelatedCreature.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
    }

    @Test
    @DisplayName("An already untapped enchanted creature does not trigger")
    void alreadyUntappedCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new WatchfulRadstag());
        attachAura(player1, creature);
        harness.setLife(player1, 10);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.performUntapStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Two Well Rested Auras grant independent once-per-turn abilities")
    void multipleAurasTriggerIndependently() {
        Permanent creature = addCreatureReady(player1, new WatchfulRadstag());
        attachAura(player1, creature);
        attachAura(player1, creature);
        creature.tap();
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new WatchfulRadstag(), new WatchfulRadstag()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.performUntapStep(player1);
        resolveAllTriggers();
        creature.tap();
        harness.performUntapStep(player1);
        resolveAllTriggers();

        assertThat(creature.getCounters().get(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("The granted ability can trigger again on a later turn")
    void triggerLimitResetsOnNextTurn() {
        Permanent creature = addCreatureReady(player1, new WatchfulRadstag());
        attachAura(player1, creature);
        creature.tap();
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new WatchfulRadstag(), new WatchfulRadstag(), new WatchfulRadstag()));
        harness.setLibrary(player2, List.of(new WatchfulRadstag(), new WatchfulRadstag()));

        harness.performUntapStep(player1);
        resolveAllTriggers();
        creature.tap();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        resolveAllTriggers();

        assertThat(creature.getCounters().get(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new WellRested());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
