package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CytoplastRootKin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagewrightsStone.class, MinisterOfImpediments.class, CytoplastRootKin.class})
class MagewrightsStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a target creature with a tap ability")
    void untapsCreatureWithTapAbility() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new MagewrightsStone());
        Permanent minister = addCreatureReady(player1, new MinisterOfImpediments());
        minister.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, minister.getId());
        harness.passBothPriorities();

        assertThat(minister.isTapped()).isFalse();
        assertThat(stone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can untap an opponent's creature with a tap ability")
    void untapsOpponentsCreatureWithTapAbility() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new MagewrightsStone());
        Permanent minister = addCreatureReady(player2, new MinisterOfImpediments());
        minister.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, minister.getId());
        harness.passBothPriorities();

        assertThat(minister.isTapped()).isFalse();
        assertThat(stone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature whose activated ability does not have a tap cost")
    void cannotTargetCreatureWithoutTapCost() {
        harness.addToBattlefield(player1, new MagewrightsStone());
        Permanent rootKin = addCreatureReady(player1, new CytoplastRootKin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rootKin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetUntappedSummoningSickCreature() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new MagewrightsStone());
        Permanent minister = harness.addToBattlefieldAndReturn(player1, new MinisterOfImpediments());
        minister.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, minister.getId());
        harness.passBothPriorities();

        assertThat(minister.isTapped()).isFalse();
        assertThat(minister.isSummoningSick()).isTrue();
        assertThat(stone.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNoncreatureWithTapAbility() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new MagewrightsStone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, stone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new MagewrightsStone());
        Permanent minister = addCreatureReady(player1, new MinisterOfImpediments());
        minister.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, minister.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(stone.isTapped()).isFalse();
        assertThat(minister.isTapped()).isTrue();
    }

    @Test
    void cannotActivateTappedStone() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new MagewrightsStone());
        Permanent minister = addCreatureReady(player1, new MinisterOfImpediments());
        stone.tap();
        minister.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, minister.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(minister.isTapped()).isTrue();
    }

    @Test
    void doesNotUntapTargetThatLosesItsTapAbilityBeforeResolution() {
        harness.addToBattlefield(player1, new MagewrightsStone());
        Permanent minister = addCreatureReady(player1, new MinisterOfImpediments());
        minister.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, minister.getId());
        minister.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.passBothPriorities();

        assertThat(minister.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
