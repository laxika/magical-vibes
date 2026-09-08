package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NornsWellspringTest extends BaseCardTest {

    @Test
    @DisplayName("An ally creature's death scries before adding an oil counter")
    void allyDeathScriesBeforeAddingOilCounter() {
        Permanent wellspring = addReadyWellspring(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();

        killWithShock(player2, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(wellspring.getCounterCount(CounterType.OIL)).isZero();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(wellspring.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger")
    void opponentDeathDoesNotTrigger() {
        Permanent wellspring = addReadyWellspring(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithShock(player1, bears.getId());

        assertThat(wellspring.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Removing two oil counters and paying one mana draws a card")
    void removingOilCountersDrawsCard() {
        Permanent wellspring = addReadyWellspring(player1);
        wellspring.setCounterCount(CounterType.OIL, 2);
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(wellspring.getCounterCount(CounterType.OIL)).isZero();
        assertThat(wellspring.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("The draw ability cannot be activated while tapped")
    void drawAbilityRequiresUntappedWellspring() {
        Permanent wellspring = addReadyWellspring(player1);
        wellspring.setCounterCount(CounterType.OIL, 2);
        wellspring.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWellspring(Player player) {
        NornsWellspring card = new NornsWellspring();
        Permanent wellspring = new Permanent(card);
        wellspring.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(wellspring);
        return wellspring;
    }

    private void killWithShock(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
