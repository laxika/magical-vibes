package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrostpeakYeti.class})
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

    @Test
    @DisplayName("Exactly one ordinary mana and one snow mana of any color pay the ability cost")
    void exactPaymentWithNonblueSnowMana() {
        Permanent yeti = addYeti();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(yeti.isCantBeBlocked()).isFalse();
        assertThat(pool.getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(yeti.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("One snow mana cannot also pay the generic part of the ability cost")
    void requiresSeparateManaForGenericCost() {
        Permanent yeti = addYeti();
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(yeti.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("A tapped and summoning-sick Frostpeak Yeti can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent yeti = addYeti();
        yeti.setSummoningSick(true);
        yeti.tap();
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(yeti.isCantBeBlocked()).isTrue();
        assertThat(yeti.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability affects only the Frostpeak Yeti that activated it")
    void affectsOnlyItsSource() {
        Permanent yeti = addYeti();
        Permanent otherYeti = harness.addToBattlefieldAndReturn(player1, new FrostpeakYeti());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(yeti.isCantBeBlocked()).isTrue();
        assertThat(otherYeti.isCantBeBlocked()).isFalse();
    }

    private Permanent addYeti() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return addCreatureReady(player1, new FrostpeakYeti());
    }

    private void addAbilityMana() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.BLUE, 2);
        pool.addSnowMana(ManaColor.BLUE, 1);
    }
}
