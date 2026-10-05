package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.Badgermole;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KataraBendingProdigy.class, Badgermole.class})
class KataraBendingProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter at your end step while tapped")
    void getsCounterAtEndStepWhileTapped() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraBendingProdigy());
        katara.tap();

        resolveEndStep();

        assertThat(katara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a +1/+1 counter at your end step while untapped")
    void doesNotGetCounterAtEndStepWhileUntapped() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraBendingProdigy());

        resolveEndStep();

        assertThat(katara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Waterbend taps six artifacts or creatures and draws a card")
    void waterbendTapsSixPermanentsAndDraws() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraBendingProdigy());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new Badgermole());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new Badgermole());
        Permanent thirdCreature = harness.addToBattlefieldAndReturn(player1, new Badgermole());
        Permanent fourthCreature = harness.addToBattlefieldAndReturn(player1, new Badgermole());
        Permanent fifthCreature = harness.addToBattlefieldAndReturn(player1, new Badgermole());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Badgermole()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(katara.isTapped()).isTrue();
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(thirdCreature.isTapped()).isTrue();
        assertThat(fourthCreature.isTapped()).isTrue();
        assertThat(fifthCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Waterbend cannot be paid without six available payments")
    void waterbendRequiresSixPayments() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraBendingProdigy());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new Badgermole());
        harness.addToBattlefieldAndReturn(player1, new Badgermole());
        harness.addToBattlefieldAndReturn(player1, new Badgermole());
        harness.addToBattlefieldAndReturn(player1, new Badgermole());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("waterbend");

        assertThat(katara.isTapped()).isFalse();
        assertThat(firstCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untapping Katara before her end-step trigger resolves prevents the counter")
    void untappingBeforeResolutionPreventsCounter() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraBendingProdigy());
        katara.tap();
        beginEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        katara.untap();
        harness.passBothPriorities();

        assertThat(katara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Tapping Katara after the end step begins does not create a trigger")
    void tappingAfterEndStepBeginsDoesNotTrigger() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraBendingProdigy());
        beginEndStep(player1);
        assertThat(gd.stack).isEmpty();

        katara.tap();

        assertThat(gd.stack).isEmpty();
        assertThat(katara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Katara does not get a counter at an opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraBendingProdigy());
        katara.tap();

        beginEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(katara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Waterbend can be paid entirely with mana while Katara is already tapped")
    void waterbendWithManaWhileTapped() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraBendingProdigy());
        katara.tap();
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setHand(player1, List.of());
        Badgermole drawnCard = new Badgermole();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(katara.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Waterbend combines mana and tapping a summoning-sick creature")
    void waterbendCombinesManaAndTappingKatara() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraBendingProdigy());
        katara.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setHand(player1, List.of());
        Badgermole drawnCard = new Badgermole();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        assertThat(katara.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        resolveEndStep();
        assertThat(katara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapped creatures and opposing creatures cannot pay Katara's waterbend cost")
    void waterbendExcludesTappedAndOpposingCreatures() {
        Permanent katara = harness.addToBattlefieldAndReturn(player1, new KataraBendingProdigy());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new Badgermole());
        tappedCreature.tap();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new Badgermole());
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("waterbend");

        assertThat(katara.isTapped()).isFalse();
        assertThat(tappedCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void beginEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private void resolveEndStep() {
        beginEndStep(player1);
        harness.passBothPriorities();
    }
}
