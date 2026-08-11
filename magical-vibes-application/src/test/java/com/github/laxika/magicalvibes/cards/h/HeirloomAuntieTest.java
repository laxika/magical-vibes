package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HeirloomAuntieTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two -1/-1 counters")
    void entersWithTwoMinusOneMinusOneCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HeirloomAuntie()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Heirloom Auntie")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An ally creature's death surveils before removing a counter")
    void allyDeathSurveilsThenRemovesCounter() {
        Permanent auntie = addReadyAuntie(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        killWithShock(player2, bears.getId());
        harness.passBothPriorities();

        assertThat(auntie.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(auntie.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining surveil still removes a -1/-1 counter")
    void decliningSurveilStillRemovesCounter() {
        Permanent auntie = addReadyAuntie(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        killWithShock(player2, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(auntie.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger Heirloom Auntie")
    void opponentDeathDoesNotTrigger() {
        Permanent auntie = addReadyAuntie(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithShock(player1, bears.getId());

        assertThat(auntie.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyAuntie(Player player) {
        HeirloomAuntie card = new HeirloomAuntie();
        Permanent auntie = new Permanent(card);
        auntie.setSummoningSick(false);
        auntie.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        gd.playerBattlefields.get(player.getId()).add(auntie);
        return auntie;
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
