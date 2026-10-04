package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.FleetingDistraction;
import com.github.laxika.magicalvibes.cards.t.TwinningStaff;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EchoMage.class, CounselOfTheSoratami.class, FleetingDistraction.class, TwinningStaff.class})
class EchoMageTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Echo Mage's toughness at both thresholds")
    void levelsUpAtThresholds() {
        Permanent mage = addCreatureReady(player1, new EchoMage());
        prepareForLeveling(player1, 8);

        levelUp(player1);
        levelUp(player1);

        assertThat(mage.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
        assertStats(mage, 2, 4);

        levelUp(player1);
        levelUp(player1);

        assertThat(mage.getCounterCount(CounterType.LEVEL)).isEqualTo(4);
        assertStats(mage, 2, 5);
    }

    @Test
    @DisplayName("At levels two through three Echo Mage copies a target instant or sorcery once")
    void copiesOnceAtLevelsTwoThroughThree() {
        Permanent mage = addCreatureReady(player1, new EchoMage());
        prepareForLeveling(player1, 20);
        levelUp(player1);
        levelUp(player1);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 1, null, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("At level four Echo Mage copies a target instant or sorcery twice")
    void copiesTwiceAtLevelFour() {
        Permanent mage = addCreatureReady(player1, new EchoMage());
        prepareForLeveling(player1, 20);
        for (int i = 0; i < 4; i++) {
            levelUp(player1);
        }

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 1, null, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    void levelUpRequiresAnEmptyStack() {
        Permanent mage = addCreatureReady(player1, new EchoMage());
        prepareForLeveling(player1, 4);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mage.getCounterCount(CounterType.LEVEL)).isZero();
        harness.passBothPriorities();
        assertThat(mage.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
    }

    @Test
    void levelUpIsUnavailableOutsideTheMainPhase() {
        Permanent mage = addCreatureReady(player1, new EchoMage());
        prepareForLeveling(player1, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mage.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    void summoningSicknessDoesNotPreventLevelUp() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new EchoMage());
        mage.setSummoningSick(true);
        prepareForLeveling(player1, 2);

        levelUp(player1);

        assertThat(mage.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(mage.isTapped()).isFalse();
    }

    @Test
    void twoCopiesCanChooseTargetsIndependently() {
        Permanent mage = addCreatureReady(player1, new EchoMage());
        mage.setCounterCount(CounterType.LEVEL, 4);
        Permanent otherMage = addCreatureReady(player2, new EchoMage());
        FleetingDistraction distraction = new FleetingDistraction();
        prepareForLeveling(player1, 3);
        harness.setHand(player1, List.of(distraction));
        harness.setLibrary(player1, List.of(new EchoMage(), new EchoMage(), new EchoMage()));
        harness.castInstant(player1, 0, mage.getId());
        harness.activateAbility(player1, 0, 1, null, distraction.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherMage.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        assertThat(gd.stack.stream().filter(StackEntry::isCopy).map(StackEntry::getTargetId).toList())
                .containsExactlyInAnyOrder(mage.getId(), otherMage.getId());
        resolveAllTriggers();
        assertStats(mage, 0, 5);
        assertStats(otherMage, 1, 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(distraction);
    }

    @Test
    void copyingTwiceWithTwinningStaffCreatesThreeCopies() {
        Permanent mage = addCreatureReady(player1, new EchoMage());
        mage.setCounterCount(CounterType.LEVEL, 4);
        harness.addToBattlefield(player1, new TwinningStaff());
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        prepareForLeveling(player1, 5);
        harness.setHand(player1, List.of(counsel));
        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 1, null, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(3);
    }

    private void prepareForLeveling(Player player, int mana) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.BLUE, mana);
    }

    private void levelUp(Player player) {
        harness.activateAbility(player, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
