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
        answerRemovals(new Removal(ownCreature.getId(), 2), new Removal(defendingCreature.getId(), 1));

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
        answerRemovals(new Removal(ownCreature.getId(), 1));

        assertThat(ownCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(defendingCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Choosing zero counters after accepting removal draws no cards")
    void choosesZeroCounters() {
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new SensationalSpiderMan());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        answerRemovals();

        assertThat(defender.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Removal stops at three even when more stun counters remain")
    void limitsRemovalToThree() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new SensationalSpiderMan());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());
        defender.setCounterCount(CounterType.STUN, 4);
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.ColorChoice question =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(question.options()).containsExactly("0", "1", "2", "3");
        answerRemovals(new Removal(defender.getId(), 3));

        assertThat(defender.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("Removal includes noncreature permanents and leaves other counter types alone")
    void removesStunCountersFromLand() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new SensationalSpiderMan());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.setCounterCount(CounterType.STUN, 1);
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        answerRemovals(new Removal(land.getId(), 1), new Removal(defender.getId(), 1));

        assertThat(land.getCounterCount(CounterType.STUN)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(defender.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("An already tapped target still receives a stun counter that can immediately be removed")
    void stunsAlreadyTappedTarget() {
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new SensationalSpiderMan());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());
        defender.tap();
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        harness.passBothPriorities();
        assertThat(defender.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.handleMayAbilityChosen(player1, true);
        answerRemovals(new Removal(defender.getId(), 1));

        assertThat(defender.isTapped()).isTrue();
        assertThat(defender.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("The attack trigger resolves after Spider-Man leaves the battlefield")
    void resolvesWithoutSource() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent spiderMan = addCreatureReady(player1, new SensationalSpiderMan());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        gd.playerBattlefields.get(player1.getId()).remove(spiderMan);
        gd.playerGraveyards.get(player1.getId()).add(spiderMan.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        answerRemovals(new Removal(defender.getId(), 1));

        assertThat(defender.isTapped()).isTrue();
        assertThat(defender.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Losing the sole target prevents removal and drawing")
    void doesNotResolveWithoutTarget() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new SensationalSpiderMan());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.STUN, 2);
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defender.getId());
        gd.playerBattlefields.get(player2.getId()).remove(defender);
        gd.playerGraveyards.get(player2.getId()).add(defender.getCard());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private record Removal(UUID permanentId, int amount) {
    }

    /** Answers each "how many stun counters to remove" question with the listed amount (zero if unlisted). */
    private void answerRemovals(Removal... removals) {
        while (gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class) != null
                && gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context()
                instanceof ChoiceContext.MoveCountersAmountChoice context) {
            ChoiceContext.CounterMoveSlot slot = context.slots().get(context.index());
            assertThat(slot.toPermanentId()).isNull();
            assertThat(slot.counterType()).isEqualTo(CounterType.STUN);
            int amount = 0;
            for (Removal removal : removals) {
                if (removal.permanentId().equals(slot.fromPermanentId())) {
                    amount = removal.amount();
                }
            }
            harness.handleListChoice(player1, Integer.toString(amount));
        }
    }
}
