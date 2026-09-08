package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RumblingCrescendoTest extends BaseCardTest {

    @Test
    void upkeepMayAddVerseCounter() {
        Permanent crescendo = addCrescendo(0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(crescendo.getCounterCount(CounterType.VERSE)).isEqualTo(1);
    }

    @Test
    void destroysUpToVerseCounterLandsAndSacrificesCrescendo() {
        addCrescendo(2);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Island");
        harness.assertInGraveyard(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Rumbling Crescendo");
    }

    @Test
    void cannotChooseMoreTargetsThanVerseCounters() {
        addCrescendo(1);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between 0 and 1 targets");
    }

    @Test
    void cannotTargetNonlandPermanent() {
        addCrescendo(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("lands");
    }

    @Test
    void withNoVerseCountersAbilitySacrificesCrescendoWithoutDestroyingLands() {
        addCrescendo(0);
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rumbling Crescendo");
        harness.assertOnBattlefield(player2, "Island");
    }

    private Permanent addCrescendo(int verseCounters) {
        Permanent crescendo = harness.addToBattlefieldAndReturn(player1, new RumblingCrescendo());
        crescendo.setCounterCount(CounterType.VERSE, verseCounters);
        return crescendo;
    }
}
