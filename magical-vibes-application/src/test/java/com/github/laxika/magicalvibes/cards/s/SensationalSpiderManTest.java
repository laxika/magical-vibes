package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SensationalSpiderMan.class, Forest.class, GrizzlyBears.class})
class SensationalSpiderManTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps and stuns the defending creature, then draws for removed stun counters")
    void attacksAndDrawsForCountersRemovedFromAllPermanents() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent spiderMan = addCreatureReady(player1, new SensationalSpiderMan());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.STUN, 2);
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        choosePermanentById(ownCreature.getId());
        choosePermanentById(ownCreature.getId());
        choosePermanentById(defendingCreature.getId());

        assertThat(spiderMan.isTapped()).isTrue();
        assertThat(defendingCreature.isTapped()).isTrue();
        assertThat(defendingCreature.getCounterCount(CounterType.STUN)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 3);
    }

    @Test
    @DisplayName("Declining the optional counter removal does not draw")
    void declinesCounterRemoval() {
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new SensationalSpiderMan());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(defendingCreature.isTapped()).isTrue();
        assertThat(defendingCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Stopping early draws only for the counters actually removed")
    void stopsEarly() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new SensationalSpiderMan());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.STUN, 2);
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        choosePermanentById(ownCreature.getId());
        harness.handleListChoice(player1, ChoiceContext.RemoveUpToCountersFromAllPermanentsChoice.DONE);

        assertThat(ownCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(defendingCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    private void choosePermanentById(UUID permanentId) {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        ChoiceContext.RemoveUpToCountersFromAllPermanentsChoice context =
                (ChoiceContext.RemoveUpToCountersFromAllPermanentsChoice) choice.context();
        String option = context.permanentOptions().entrySet().stream()
                .filter(entry -> entry.getValue().equals(permanentId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
        harness.handleListChoice(player1, option);
    }
}
