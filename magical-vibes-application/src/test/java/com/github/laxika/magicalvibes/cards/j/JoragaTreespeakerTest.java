package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JoragaTreespeaker.class, NestInvader.class})
class JoragaTreespeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up Joraga Treespeaker changes its power and toughness")
    void levelsUpAtThresholds() {
        Permanent treespeaker = addCreatureReady(player1, new JoragaTreespeaker());

        assertStats(treespeaker, 1, 1);

        prepareForLeveling(player1, 10);
        levelUp(player1, 1);
        assertThat(treespeaker.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(treespeaker, 1, 2);

        levelUp(player1, 3);
        assertThat(treespeaker.getCounterCount(CounterType.LEVEL)).isEqualTo(4);
        assertStats(treespeaker, 1, 2);

        levelUp(player1, 1);
        assertThat(treespeaker.getCounterCount(CounterType.LEVEL)).isEqualTo(5);
        assertStats(treespeaker, 1, 4);
    }

    @Test
    @DisplayName("At levels one through four Joraga Treespeaker can add two green mana")
    void addsTwoManaAtLevelsOneThroughFour() {
        Permanent treespeaker = addCreatureReady(player1, new JoragaTreespeaker());
        prepareForLeveling(player1, 2);
        levelUp(player1, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(treespeaker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("At level five Joraga Treespeaker grants its mana ability to Elves you control")
    void grantsManaAbilityToElvesAtLevelFive() {
        addCreatureReady(player1, new JoragaTreespeaker());
        Permanent otherTreespeaker = addCreatureReady(player1, new JoragaTreespeaker());
        prepareForLeveling(player1, 10);
        levelUp(player1, 5);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(otherTreespeaker.isTapped()).isTrue();
    }

    @Test
    void levelZeroHasNoManaAbility() {
        addCreatureReady(player1, new JoragaTreespeaker());
        prepareForLeveling(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void levelFiveAndAboveCanTapTreespeakerItselfForMana() {
        Permanent treespeaker = addCreatureReady(player1, new JoragaTreespeaker());
        prepareForLeveling(player1, 12);
        levelUp(player1, 6);

        assertStats(treespeaker, 1, 4);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(treespeaker.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaGrantExcludesNonElvesAndOpposingElves() {
        addCreatureReady(player1, new JoragaTreespeaker());
        Permanent nonElf = addCreatureReady(player1, new NestInvader());
        Permanent opposingElf = addCreatureReady(player2, new JoragaTreespeaker());
        prepareForLeveling(player1, 10);
        levelUp(player1, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(nonElf.isTapped()).isFalse();
        assertThat(opposingElf.isTapped()).isFalse();
    }

    @Test
    void grantedAbilityDisappearsWhenSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new JoragaTreespeaker());
        addCreatureReady(player1, new JoragaTreespeaker());
        prepareForLeveling(player1, 10);
        levelUp(player1, 5);

        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void levelUpUsesStackAndCannotBeActivatedInResponse() {
        Permanent treespeaker = addCreatureReady(player1, new JoragaTreespeaker());
        prepareForLeveling(player1, 4);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(treespeaker.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(treespeaker.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
    }

    @Test
    void summoningSickTreespeakerCanLevelUpButCannotTapForMana() {
        Permanent treespeaker = harness.addToBattlefieldAndReturn(player1, new JoragaTreespeaker());
        treespeaker.setSummoningSick(true);
        prepareForLeveling(player1, 2);
        levelUp(player1, 1);

        assertThat(treespeaker.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(treespeaker.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void levelUpCannotBeActivatedOutsideOwnMainPhase() {
        addCreatureReady(player1, new JoragaTreespeaker());
        prepareForLeveling(player1, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void prepareForLeveling(Player player, int greenMana) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.GREEN, greenMana);
    }

    private void levelUp(Player player, int times) {
        for (int i = 0; i < times; i++) {
            harness.activateAbility(player, 0, 0, null, null);
            harness.passBothPriorities();
        }
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
