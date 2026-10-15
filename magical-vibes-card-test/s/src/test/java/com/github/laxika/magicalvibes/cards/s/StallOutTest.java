package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.u.UnswervingSloth;
import com.github.laxika.magicalvibes.cards.v.VoyagerGlidecar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StallOut.class, UnswervingSloth.class, VoyagerGlidecar.class, Forest.class})
class StallOutTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target creature and puts three stun counters on it")
    void tapsAndStunsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnswervingSloth());
        harness.setHand(player1, List.of(new StallOut()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target a noncreature Vehicle")
    void targetsNoncreatureVehicle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoyagerGlidecar());
        harness.setHand(player1, List.of(new StallOut()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature non-Vehicle permanent")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new StallOut()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or Vehicle");
    }

    @Test
    @DisplayName("Cycling discards Stall Out and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new StallOut()));
        harness.setLibrary(player1, List.of(new UnswervingSloth()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Stall Out");
        harness.assertInHand(player1, "Unswerving Sloth");
    }

    @Test
    @DisplayName("Adds stun counters even when the target is already tapped")
    void stunsAlreadyTappedVehicle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoyagerGlidecar());
        target.tap();
        harness.setHand(player1, List.of(new StallOut()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Three stun counters replace three untaps before the Vehicle untaps")
    void stunCountersReplaceThreeUntaps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoyagerGlidecar());
        harness.setHand(player1, List.of(new StallOut()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.performUntapStep(player1);
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(3);
        for (int remaining = 2; remaining >= 0; remaining--) {
            harness.performUntapStep(player2);
            assertThat(target.isTapped()).isTrue();
            assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(remaining);
        }

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cycling pays the discard cost before its draw resolves")
    void cyclingDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new StallOut()));
        harness.setLibrary(player1, List.of(new VoyagerGlidecar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Stall Out");
        harness.assertNotInHand(player1, "Stall Out");
        harness.assertNotInHand(player1, "Voyager Glidecar");

        harness.passBothPriorities();
        harness.assertInHand(player1, "Voyager Glidecar");
    }
}
