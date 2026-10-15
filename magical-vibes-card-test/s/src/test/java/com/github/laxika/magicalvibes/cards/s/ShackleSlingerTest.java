package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShackleSlinger.class, Shock.class, GrizzlyBears.class})
class ShackleSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell taps an untapped creature an opponent controls")
    void secondSpellTapsUntappedCreature() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new ShackleSlinger());
        castTwoShocks();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("The second spell puts a stun counter on a tapped creature an opponent controls")
    void secondSpellStunsTappedCreature() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        bear.tap();
        harness.addToBattlefield(player1, new ShackleSlinger());
        castTwoShocks();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void tappedStateIsCheckedAtResolution() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new ShackleSlinger());
        castTwoShocks();
        harness.handlePermanentChosen(player1, bear.getId());
        bear.tap();
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    void targetUntappedBeforeResolutionIsTappedWithoutStun() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        bear.tap();
        harness.addToBattlefield(player1, new ShackleSlinger());
        castTwoShocks();
        harness.handlePermanentChosen(player1, bear.getId());
        bear.untap();
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void firstAndThirdSpellsDoNotTrigger() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new ShackleSlinger());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bear.isTapped()).isFalse();
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        resolveAllTriggers();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bear.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void onlyOpposingCreaturesAreOfferedAsTargets() {
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ShackleSlinger());
        castTwoShocks();

        var choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(opposingBear.getId());
        assertThat(choice.validPlayerIds()).isEmpty();
        harness.handlePermanentChosen(player1, opposingBear.getId());
        resolveAllTriggers();
    }
    private void castTwoShocks() {
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
    }
}
