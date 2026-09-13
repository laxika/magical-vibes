package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Fluctuator;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.o.OpalArchangel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerrasLiturgy.class, Fluctuator.class, OpalArchangel.class, GorillaWarrior.class})
class SerrasLiturgyTest extends BaseCardTest {

    @Test
    void upkeepMayAddVerseCounter() {
        Permanent liturgy = addLiturgy(0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(liturgy.getCounterCount(CounterType.VERSE)).isEqualTo(1);
    }

    @Test
    void upkeepMayDeclineVerseCounter() {
        Permanent liturgy = addLiturgy(0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(liturgy.getCounterCount(CounterType.VERSE)).isZero();
    }

    @Test
    void destroysUpToVerseCounterArtifactsAndEnchantments() {
        addLiturgy(2);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Fluctuator());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new OpalArchangel());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(artifact.getId(), enchantment.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fluctuator");
        harness.assertNotOnBattlefield(player2, "Opal Archangel");
        harness.assertNotOnBattlefield(player1, "Serra's Liturgy");
    }

    @Test
    void mayChooseFewerTargetsThanVerseCounters() {
        addLiturgy(2);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Fluctuator());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serra's Liturgy");
        harness.assertNotOnBattlefield(player2, "Fluctuator");
    }

    @Test
    void withNoVerseCountersAbilitySacrificesWithoutDestroyingPermanents() {
        addLiturgy(0);
        harness.addToBattlefield(player2, new Fluctuator());
        harness.addToBattlefield(player2, new OpalArchangel());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serra's Liturgy");
        harness.assertOnBattlefield(player2, "Fluctuator");
        harness.assertOnBattlefield(player2, "Opal Archangel");
    }

    @Test
    void cannotChooseMoreTargetsThanVerseCounters() {
        addLiturgy(1);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new OpalArchangel());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new OpalArchangel());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between 0 and 1 targets");
    }

    @Test
    void cannotTargetNonArtifactOrNonEnchantmentPermanent() {
        addLiturgy(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Targets must be artifacts and/or enchantments");
    }

    private Permanent addLiturgy(int verseCounters) {
        Permanent liturgy = harness.addToBattlefieldAndReturn(player1, new SerrasLiturgy());
        liturgy.setCounterCount(CounterType.VERSE, verseCounters);
        harness.addMana(player1, ManaColor.WHITE, 1);
        return liturgy;
    }
}
