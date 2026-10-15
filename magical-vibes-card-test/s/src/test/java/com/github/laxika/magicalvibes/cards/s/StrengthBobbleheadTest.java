package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.InfestingRadroach;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrengthBobblehead.class, InfestingRadroach.class})
class StrengthBobbleheadTest extends BaseCardTest {

    @Test
    void manaAbilityAddsChosenColor() {
        Permanent bobblehead = harness.addToBattlefieldAndReturn(player1, new StrengthBobblehead());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(bobblehead.isTapped()).isTrue();
    }

    @Test
    void putsOneCounterPerBobbleheadOnTargetCreature() {
        harness.addToBattlefield(player1, new StrengthBobblehead());
        harness.addToBattlefield(player1, new StrengthBobblehead());
        Permanent target = addCreatureReady(player1, new InfestingRadroach());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void counterAbilityIsSorcerySpeed() {
        harness.addToBattlefield(player1, new StrengthBobblehead());
        Permanent target = addCreatureReady(player1, new InfestingRadroach());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void counterAbilityCannotTargetAnArtifact() {
        Permanent bobblehead = harness.addToBattlefieldAndReturn(player1, new StrengthBobblehead());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bobblehead.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsOnlyControllersBobbleheadsAndCanTargetOpponentsCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new StrengthBobblehead());
        harness.addToBattlefield(player2, new StrengthBobblehead());
        Permanent target = addCreatureReady(player2, new InfestingRadroach());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void countsBobbleheadsAddedBeforeResolution() {
        harness.addToBattlefield(player1, new StrengthBobblehead());
        Permanent target = addCreatureReady(player1, new InfestingRadroach());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.addToBattlefield(player1, new StrengthBobblehead());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void resolvesWithZeroCountersIfLastBobbleheadLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new StrengthBobblehead());
        Permanent target = addCreatureReady(player1, new InfestingRadroach());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterAbilityCannotBeActivatedWithNonemptyStack() {
        harness.addToBattlefield(player1, new StrengthBobblehead());
        harness.addToBattlefield(player1, new StrengthBobblehead());
        Permanent target = addCreatureReady(player1, new InfestingRadroach());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
