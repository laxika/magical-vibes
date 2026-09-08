package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VileRequiemTest extends BaseCardTest {

    @Test
    void upkeepMayAddVerseCounter() {
        Permanent requiem = addRequiem(0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(requiem.getCounterCount(CounterType.VERSE)).isEqualTo(1);
    }

    @Test
    void destroysUpToVerseCounterNonblackCreaturesWithoutRegeneration() {
        addRequiem(2);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());
        first.setRegenerationShield(1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Mass of Ghouls");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blackCreature);
        harness.assertInGraveyard(player1, "Vile Requiem");
    }

    @Test
    void cannotChooseMoreTargetsThanVerseCounters() {
        addRequiem(1);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between 0 and 1 targets");
    }

    @Test
    void cannotTargetBlackCreature() {
        addRequiem(1);
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(blackCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    void withNoVerseCountersAbilitySacrificesRequiemWithoutDestroyingCreatures() {
        addRequiem(0);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vile Requiem");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addRequiem(int verseCounters) {
        Permanent requiem = harness.addToBattlefieldAndReturn(player1, new VileRequiem());
        requiem.setCounterCount(CounterType.VERSE, verseCounters);
        return requiem;
    }
}
