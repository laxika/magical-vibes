package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SewerCrocodile.class, AirElemental.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class})
class SewerCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("Five distinct mana values reduce Sewer Crocodile's activation cost")
    void distinctManaValuesReduceActivationCost() {
        Permanent crocodile = addReadyCrocodile();
        harness.setGraveyard(player1, List.of(
                new Forest(), new LlanowarElves(), new GrizzlyBears(), new HillGiant(), new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(crocodile.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Duplicate mana values do not satisfy Sewer Crocodile's cost reduction")
    void duplicateManaValuesDoNotReduceActivationCost() {
        addReadyCrocodile();
        harness.setGraveyard(player1, List.of(
                new Forest(), new LlanowarElves(), new GrizzlyBears(), new HillGiant(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sewer Crocodile's unblockable ability wears off at cleanup")
    void unblockableAbilityWearsOffAtCleanup() {
        Permanent crocodile = addReadyCrocodile();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(crocodile.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(crocodile.isCantBeBlocked()).isFalse();
    }

    private Permanent addReadyCrocodile() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new SewerCrocodile());
        crocodile.setSummoningSick(false);
        return crocodile;
    }
}
