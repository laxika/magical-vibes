package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeritageDruid.class, ElvishWarrior.class, BallyrushBanneret.class})
class HeritageDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping three untapped Elves adds {G}{G}{G}")
    void tapThreeElvesForGreenMana() {
        Permanent source = addCreatureReady(player1, new HeritageDruid());
        Permanent elfA = addCreatureReady(player1, new ElvishWarrior());
        Permanent elfB = addCreatureReady(player1, new ElvishWarrior());

        int sourceIdx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIdx, 0, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThat(elfA.isTapped()).isTrue();
        assertThat(elfB.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate with fewer than three untapped Elves")
    void cannotActivateWithoutThreeElves() {
        Permanent source = addCreatureReady(player1, new HeritageDruid());
        addCreatureReady(player1, new ElvishWarrior());

        int sourceIdx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIdx, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    void summoningSickElvesCanPayTheCost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HeritageDruid());
        Permanent elfA = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        Permanent elfB = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThat(elfA.isTapped()).isTrue();
        assertThat(elfB.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    void tappedDruidCanActivateUsingThreeOtherElves() {
        Permanent source = addCreatureReady(player1, new HeritageDruid());
        source.tap();
        Permanent elfA = addCreatureReady(player1, new ElvishWarrior());
        Permanent elfB = addCreatureReady(player1, new ElvishWarrior());
        Permanent elfC = addCreatureReady(player1, new ElvishWarrior());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThat(elfA.isTapped()).isTrue();
        assertThat(elfB.isTapped()).isTrue();
        assertThat(elfC.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    void canChooseOtherElvesAndLeaveDruidUntapped() {
        Permanent source = addCreatureReady(player1, new HeritageDruid());
        Permanent elfA = addCreatureReady(player1, new ElvishWarrior());
        Permanent elfB = addCreatureReady(player1, new ElvishWarrior());
        Permanent elfC = addCreatureReady(player1, new ElvishWarrior());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.handlePermanentChosen(player1, elfA.getId());
        harness.handlePermanentChosen(player1, elfB.getId());
        harness.handlePermanentChosen(player1, elfC.getId());

        assertThat(source.isTapped()).isFalse();
        assertThat(elfA.isTapped()).isTrue();
        assertThat(elfB.isTapped()).isTrue();
        assertThat(elfC.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    void tappedElvesCannotPayTheCost() {
        Permanent source = addCreatureReady(player1, new HeritageDruid());
        Permanent elfA = addCreatureReady(player1, new ElvishWarrior());
        Permanent elfB = addCreatureReady(player1, new ElvishWarrior());
        elfB.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(elfA.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void opposingElvesCannotPayTheCost() {
        Permanent source = addCreatureReady(player1, new HeritageDruid());
        Permanent ownElf = addCreatureReady(player1, new ElvishWarrior());
        Permanent opposingElf = addCreatureReady(player2, new ElvishWarrior());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(ownElf.isTapped()).isFalse();
        assertThat(opposingElf.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void nonElvesCannotPayTheCost() {
        Permanent source = addCreatureReady(player1, new HeritageDruid());
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());
        Permanent nonElf = addCreatureReady(player1, new BallyrushBanneret());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(elf.isTapped()).isFalse();
        assertThat(nonElf.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
