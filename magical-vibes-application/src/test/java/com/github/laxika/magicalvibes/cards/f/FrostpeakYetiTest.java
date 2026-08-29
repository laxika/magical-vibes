package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FrostpeakYetiTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability requires snow mana")
    void requiresSnowMana() {
        Permanent yeti = addYeti();
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(yeti.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The activated ability makes Frostpeak Yeti unblockable this turn")
    void abilityMakesSelfUnblockable() {
        Permanent yeti = addYeti();
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(yeti.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        Permanent yeti = addYeti();
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(yeti.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(yeti.isCantBeBlocked()).isFalse();
    }

    private Permanent addYeti() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent yeti = harness.addToBattlefieldAndReturn(player1, new FrostpeakYeti());
        yeti.setSummoningSick(false);
        return yeti;
    }

    private void addAbilityMana() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.BLUE, 2);
        pool.addSnowMana(ManaColor.BLUE, 1);
    }
}
