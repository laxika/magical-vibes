package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LilysplashMentor.class, GrizzlyBears.class})
class LilysplashMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers another creature you control and returns it with a +1/+1 counter")
    void flickersOwnCreatureWithCounter() {
        addReadyMentor();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target Lilysplash Mentor itself")
    void cannotTargetItself() {
        Permanent mentor = addReadyMentor();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, mentor.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        addReadyMentor();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can only be activated at sorcery speed")
    void cannotActivateOnOpponentsTurn() {
        addReadyMentor();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadyMentor() {
        return addCreatureReady(player1, new LilysplashMentor());
    }
}
