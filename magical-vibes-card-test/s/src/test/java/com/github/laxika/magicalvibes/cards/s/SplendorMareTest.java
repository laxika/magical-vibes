package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrannithHealer;
import com.github.laxika.magicalvibes.cards.f.Farfinder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SplendorMare.class, DrannithHealer.class, Farfinder.class})
class SplendorMareTest extends BaseCardTest {

    @Test
    void cyclingPutsLifelinkCounterOnTargetCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Farfinder());
        prepareCycling();

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Splendor Mare");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        harness.assertNotInHand(player1, "Farfinder");
        resolveAllTriggers();
        harness.assertInHand(player1, "Farfinder");
    }

    @Test
    void cyclingCannotTargetCreatureAnOpponentControls() {
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new Farfinder());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Farfinder());
        prepareCycling();

        harness.activateHandAbility(player1, 0, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, friendly.getId());
        resolveAllTriggers();

        assertThat(friendly.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    void battlefieldMareDoesNotTriggerWhenAnotherCardIsCycled() {
        Permanent mare = harness.addToBattlefieldAndReturn(player1, new SplendorMare());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Farfinder());
        harness.setHand(player1, List.of(new DrannithHealer()));
        harness.setLibrary(player1, List.of(new Farfinder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(mare.getCounterCount(CounterType.LIFELINK)).isZero();
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isZero();
        harness.assertInHand(player1, "Farfinder");
    }

    @Test
    void cyclingWithoutAnyLegalTargetStillDraws() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Farfinder());
        prepareCycling();

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(opponent.getCounterCount(CounterType.LIFELINK)).isZero();
        harness.assertInGraveyard(player1, "Splendor Mare");
        harness.assertInHand(player1, "Farfinder");
    }

    private void prepareCycling() {
        harness.setHand(player1, List.of(new SplendorMare()));
        harness.setLibrary(player1, List.of(new Farfinder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
