package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindingConstrictor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({T45PowerArmor.class, GrizzlyBears.class, WindingConstrictor.class})
class T45PowerArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gives its controller two energy counters")
    void entersWithTwoEnergyCounters() {
        harness.setHand(player1, List.of(new T45PowerArmor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature gets +3/+3 and remains tapped during its controller's untap step")
    void equippedCreatureGetsBoostAndDoesNotUntap() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new T45PowerArmor());
        armor.setAttachedTo(creature.getId());
        creature.tap();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "Put a menace counter on equipped creature, MENACE, MENACE",
            "Put a trample counter on equipped creature, TRAMPLE, TRAMPLE",
            "Put a lifelink counter on equipped creature, LIFELINK, LIFELINK"
    })
    @DisplayName("Paying one energy untaps the equipped creature and adds the chosen keyword counter")
    void paysEnergyForUntapAndKeywordCounter(String mode, CounterType counterType, Keyword keyword) {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new T45PowerArmor());
        armor.setAttachedTo(creature.getId());
        creature.tap();
        gd.playerEnergyCounters.put(player1.getId(), 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, mode);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(counterType)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, keyword)).isTrue();
    }

    @Test
    @DisplayName("Declining the upkeep payment preserves energy and leaves the creature tapped")
    void declinesEnergyPayment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new T45PowerArmor());
        armor.setAttachedTo(creature.getId());
        creature.tap();
        gd.playerEnergyCounters.put(player1.getId(), 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(creature.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(creature.getCounterCount(CounterType.LIFELINK)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Without energy the upkeep ability neither untaps nor adds a counter")
    void cannotPayWithoutEnergy() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new T45PowerArmor());
        armor.setAttachedTo(creature.getId());
        creature.tap();
        gd.playerEnergyCounters.put(player1.getId(), 0);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(creature.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(creature.getCounterCount(CounterType.LIFELINK)).isZero();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Paying energy puts a counter on an already untapped creature and applies counter replacements")
    void putsCounterOnUntappedCreature() {
        Permanent creature = addCreatureReady(player1, new WindingConstrictor());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new T45PowerArmor());
        armor.setAttachedTo(creature.getId());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Put a menace counter on equipped creature");

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.MENACE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();

        armor.setAttachedTo(null);
        assertThat(creature.getCounterCount(CounterType.MENACE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The upkeep ability affects the creature equipped when it resolves")
    void usesCurrentAttachmentAtResolution() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new T45PowerArmor());
        armor.setAttachedTo(first.getId());
        first.tap();
        gd.playerEnergyCounters.put(player1.getId(), 1);

        advanceToUpkeep(player1);
        armor.setAttachedTo(second.getId());
        second.tap();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Put a trample counter on equipped creature");

        assertThat(first.isTapped()).isTrue();
        assertThat(first.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(second.isTapped()).isFalse();
        assertThat(second.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the Equipment controller's upkeep triggers its energy payment")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new T45PowerArmor());
        armor.setAttachedTo(creature.getId());
        creature.tap();
        gd.playerEnergyCounters.put(player1.getId(), 1);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Put a lifelink counter on equipped creature");

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip costs three mana and moves the boost to the new creature")
    void equipsAndMovesBoost() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new T45PowerArmor());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, first.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);

        harness.activateAbility(player1, 0, null, second.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
    }

}
