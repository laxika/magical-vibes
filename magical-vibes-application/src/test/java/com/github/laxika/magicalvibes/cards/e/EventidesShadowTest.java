package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EventidesShadow.class, Forest.class, GrizzlyBears.class})
class EventidesShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Removes chosen counters from any permanents, then draws and loses life for them")
    void removesCountersDrawsAndLosesLife() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        ownCreature.setCounterCount(CounterType.CHARGE, 1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.LORE, 1);

        castEventidesShadow();
        chooseCounter(ownCreature.getId(), CounterType.PLUS_ONE_PLUS_ONE);
        chooseCounter(ownCreature.getId(), CounterType.CHARGE);
        chooseCounter(opponentCreature.getId(), CounterType.LORE);
        harness.handleListChoice(player1,
                ChoiceContext.RemoveAnyNumberOfCountersFromAllPermanentsChoice.DONE);

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.LORE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Choosing Done removes no counters and produces no draw or life loss")
    void canChooseNoCounters() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castEventidesShadow();
        harness.handleListChoice(player1,
                ChoiceContext.RemoveAnyNumberOfCountersFromAllPermanentsChoice.DONE);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prompt when no permanent has counters")
    void noPromptWithoutCounters() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new GrizzlyBears());

        castEventidesShadow();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private void castEventidesShadow() {
        harness.setHand(player1, List.of(new EventidesShadow()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void chooseCounter(UUID permanentId, CounterType counterType) {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        ChoiceContext.RemoveAnyNumberOfCountersFromAllPermanentsChoice context =
                (ChoiceContext.RemoveAnyNumberOfCountersFromAllPermanentsChoice) choice.context();
        String option = context.counterOptions().entrySet().stream()
                .filter(entry -> entry.getValue().permanentId().equals(permanentId)
                        && entry.getValue().counterType() == counterType)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
        harness.handleListChoice(player1, option);
    }
}
