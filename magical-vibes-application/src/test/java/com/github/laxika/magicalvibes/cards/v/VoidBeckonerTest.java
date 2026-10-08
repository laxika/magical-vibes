package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.p.PhaseDolphin;
import com.github.laxika.magicalvibes.cards.h.HeartlessAct;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoidBeckoner.class, PhaseDolphin.class, HeartlessAct.class})
class VoidBeckonerTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling puts a deathtouch counter on a creature you control and draws a card")
    void cyclingPutsDeathtouchCounterOnOwnCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PhaseDolphin());
        harness.setHand(player1, List.of(new VoidBeckoner()));
        harness.setLibrary(player1, List.of(new PhaseDolphin()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        harness.assertInGraveyard(player1, "Void Beckoner");
        harness.assertInHand(player1, "Phase Dolphin");
    }

    @Test
    @DisplayName("Cycling with no legal creature you control still draws a card")
    void cyclingWithoutLegalTargetStillDraws() {
        harness.addToBattlefield(player2, new PhaseDolphin());
        harness.setHand(player1, List.of(new VoidBeckoner()));
        harness.setLibrary(player1, List.of(new PhaseDolphin()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Void Beckoner");
        harness.assertInHand(player1, "Phase Dolphin");
    }

    @Test
    @DisplayName("The cycling trigger cannot target an opponent's creature")
    void cyclingCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhaseDolphin());
        harness.setHand(player1, List.of(new VoidBeckoner()));
        harness.setLibrary(player1, List.of(new PhaseDolphin()));
        addCyclingMana();

        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new PhaseDolphin());
        harness.activateHandAbility(player1, 0, null);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(ownTarget.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        harness.assertInHand(player1, "Phase Dolphin");
    }

    @Test
    @DisplayName("The cycling trigger resolves before the separate draw ability")
    void counterTriggerResolvesBeforeDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PhaseDolphin());
        harness.setHand(player1, List.of(new VoidBeckoner()));
        harness.setLibrary(player1, List.of(new PhaseDolphin()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, target.getId());
        harness.assertInGraveyard(player1, "Void Beckoner");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        harness.assertNotInHand(player1, "Phase Dolphin");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Phase Dolphin");
    }

    @Test
    @DisplayName("Removing the counter trigger's target does not stop the cycling draw")
    void removedTargetDoesNotStopDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PhaseDolphin());
        harness.setHand(player1, List.of(new VoidBeckoner()));
        harness.setLibrary(player1, List.of(new PhaseDolphin()));
        addCyclingMana();
        harness.activateHandAbility(player1, 0, target.getId());

        harness.setHand(player2, List.of(new HeartlessAct()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Phase Dolphin");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Phase Dolphin");
        assertThat(target.getCounterCount(CounterType.DEATHTOUCH)).isZero();
    }

    @Test
    @DisplayName("Cycling requires the counter trigger to target an available creature")
    void availableCreatureCannotBeSkipped() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PhaseDolphin());
        harness.setHand(player1, List.of(new VoidBeckoner()));
        harness.setLibrary(player1, List.of(new PhaseDolphin()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        harness.assertInHand(player1, "Phase Dolphin");
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
