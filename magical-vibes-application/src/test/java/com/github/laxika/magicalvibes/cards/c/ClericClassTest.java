package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClericClass.class, GrizzlyBears.class})
class ClericClassTest extends BaseCardTest {

    @Test
    void gainsAdditionalLifeAtLevelOne() {
        harness.addToBattlefield(player1, new ClericClass());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 24);
    }

    @Test
    void levelTwoPutsCounterOnTargetCreatureYouControl() {
        Permanent clericClass = harness.addToBattlefieldAndReturn(player1, new ClericClass());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        prepareForAbility(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(clericClass.getClassLevel()).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 23);
    }

    @Test
    void levelThreeReturnsCreatureAndGainsItsToughnessAsLife() {
        harness.addToBattlefield(player1, new ClericClass());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        prepareForAbility(player1);
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Grizzly Bears").getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        harness.assertLife(player1, 23);
    }

    @Test
    void levelOneDoesNotHaveTheCounterTrigger() {
        harness.addToBattlefield(player1, new ClericClass());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareForAbility(player1);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentLifeGainIsNeitherIncreasedNorTriggered() {
        harness.addToBattlefield(player1, new ClericClass());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareForAbility(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));
        harness.passBothPriorities();

        harness.assertLife(player2, 23);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void gainingZeroLifeDoesNotBecomePositiveLifeGain() {
        harness.addToBattlefield(player1, new ClericClass());
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareForAbility(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 0));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSkipLevelTwoOrActivateItAgain() {
        harness.addToBattlefield(player1, new ClericClass());
        prepareForAbility(player1);
        harness.addMana(player1, ManaColor.WHITE, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotGainALevelDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new ClericClass());
        prepareForAbility(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareForAbility(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
