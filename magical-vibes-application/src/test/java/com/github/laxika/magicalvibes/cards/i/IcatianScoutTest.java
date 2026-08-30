package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcatianScout.class, IcatianStore.class})
class IcatianScoutTest extends BaseCardTest {

    @Test
    @DisplayName("{1}, {T} ability grants first strike to the target creature")
    void grantsFirstStrikeToTarget() {
        Permanent scout = addCreatureReady(player1, new IcatianScout());
        Permanent target = addCreatureReady(player1, new IcatianScout());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(scout.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{1}, {T} ability can target Icatian Scout itself")
    void canTargetItself() {
        Permanent scout = addCreatureReady(player1, new IcatianScout());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, scout.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scout, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("{1}, {T} ability can target an opponent's creature")
    void canTargetOpponentsCreature() {
        addCreatureReady(player1, new IcatianScout());
        Permanent target = addCreatureReady(player2, new IcatianScout());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOff() {
        addCreatureReady(player1, new IcatianScout());
        Permanent target = addCreatureReady(player1, new IcatianScout());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("{1}, {T} ability targeting a non-creature is rejected")
    void illegalTargetRejected() {
        addCreatureReady(player1, new IcatianScout());
        Permanent store = harness.addToBattlefieldAndReturn(player1, new IcatianStore());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, store.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{1}, {T} ability requires generic mana")
    void requiresGenericMana() {
        addCreatureReady(player1, new IcatianScout());
        Permanent target = addCreatureReady(player1, new IcatianScout());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{1}, {T} ability cannot be activated while Icatian Scout is tapped")
    void cannotActivateWhenTapped() {
        Permanent scout = addCreatureReady(player1, new IcatianScout());
        Permanent target = addCreatureReady(player1, new IcatianScout());
        scout.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
