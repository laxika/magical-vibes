package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UltrosObnoxiousOctopus.class, GrizzlyBears.class, Hurricane.class, Shock.class})
class UltrosObnoxiousOctopusTest extends BaseCardTest {

    @Test
    @DisplayName("A noncreature spell with less than four mana spent does not trigger Ultros")
    void cheapNoncreatureSpellDoesNotTrigger() {
        Permanent ultros = addUltros();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setUpMainPhase();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(ultros.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.isTapped()).isFalse();
        assertThat(bears.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("A noncreature spell with at least four mana spent taps and stuns an opponent creature")
    void fourManaSpellTapsAndStunsOpponentCreature() {
        Permanent ultros = addUltros();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setUpMainPhase();

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(ultros.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A noncreature spell with at least eight mana spent adds eight +1/+1 counters")
    void eightManaSpellAddsEightCountersAndTapsAndStuns() {
        Permanent ultros = addUltros();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setUpMainPhase();

        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 7);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ultros.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The triggered ability cannot target a creature you control")
    void cannotTargetOwnCreature() {
        addUltros();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setUpMainPhase();

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addUltros() {
        return harness.addToBattlefieldAndReturn(player1, new UltrosObnoxiousOctopus());
    }

    private void setUpMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
