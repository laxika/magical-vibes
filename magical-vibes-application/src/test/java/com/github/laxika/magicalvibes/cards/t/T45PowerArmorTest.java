package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({T45PowerArmor.class, GrizzlyBears.class})
class T45PowerArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gives its controller two energy counters")
    void entersWithTwoEnergyCounters() {
        harness.setHand(player1, List.of(new T45PowerArmor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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

}
