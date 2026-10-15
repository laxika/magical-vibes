package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LambholtHarrier.class, Forest.class, GrizzlyBears.class})
class LambholtHarrierTest extends BaseCardTest {

    @Test
    @DisplayName("Ability makes target creature unable to block this turn")
    void abilityMakesTargetUnableToBlock() {
        addCreatureReady(player1, new LambholtHarrier());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void abilityCannotTargetNonCreature() {
        addCreatureReady(player1, new LambholtHarrier());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can't-block effect wears off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new LambholtHarrier());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Harrier can activate and target itself")
    void tappedSummoningSickHarrierCanTargetItself() {
        Permanent harrier = harness.addToBattlefieldAndReturn(player1, new LambholtHarrier());
        harrier.setSummoningSick(true);
        harrier.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, harrier.getId());
        harness.passBothPriorities();

        assertThat(harrier.isCantBlockThisTurn()).isTrue();
        assertThat(harrier.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability can be activated repeatedly without tapping the source")
    void abilityCanBeActivatedRepeatedly() {
        Permanent harrier = addCreatureReady(player1, new LambholtHarrier());
        Permanent first = addCreatureReady(player2, new LambholtHarrier());
        Permanent second = addCreatureReady(player2, new LambholtHarrier());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(first.isCantBlockThisTurn()).isTrue();
        assertThat(second.isCantBlockThisTurn()).isTrue();
        assertThat(harrier.isCantBlockThisTurn()).isFalse();
        assertThat(harrier.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability requires the full four mana activation cost")
    void abilityCannotBeActivatedWithInsufficientMana() {
        Permanent harrier = addCreatureReady(player1, new LambholtHarrier());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, harrier.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(harrier.isCantBlockThisTurn()).isFalse();
    }
}
