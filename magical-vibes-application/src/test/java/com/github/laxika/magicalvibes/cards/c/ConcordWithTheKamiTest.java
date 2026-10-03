package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConcordWithTheKami.class, GrizzlyBears.class, Pacifism.class, Bonesplitter.class})
class ConcordWithTheKamiTest extends BaseCardTest {

    private static final String COUNTER_MODE =
            "Put a +1/+1 counter on target creature with a counter on it.";
    private static final String DRAW_MODE = "Draw a card if you control an enchanted creature.";
    private static final String SPIRIT_MODE =
            "Create a 1/1 colorless Spirit creature token if you control an equipped creature.";

    @Test
    void allSelectedModesResolve() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attach(new Pacifism(), creature);
        attach(new Bonesplitter(), creature);
        harness.addToBattlefield(player1, new ConcordWithTheKami());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToEndStep();
        chooseModes(COUNTER_MODE, DRAW_MODE, SPIRIT_MODE);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    void counterModeOnlyOffersCreaturesWithCounters() {
        Permanent eligible = addCreatureReady(player1, new GrizzlyBears());
        eligible.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent ineligible = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ConcordWithTheKami());

        advanceToEndStep();
        chooseModes(COUNTER_MODE);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(eligible.getId());
        harness.handlePermanentChosen(player1, eligible.getId());
        harness.passBothPriorities();

        assertThat(eligible.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ineligible.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void conditionalModesDoNothingWithoutMatchingPermanents() {
        harness.addToBattlefield(player1, new ConcordWithTheKami());
        GrizzlyBears card = new GrizzlyBears();
        harness.setLibrary(player1, List.of(card));

        advanceToEndStep();
        chooseModes(DRAW_MODE, SPIRIT_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    void modesMustBeChosenBeforePlayersCanRespond() {
        harness.addToBattlefield(player1, new ConcordWithTheKami());

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ColorChoice.class);
        chooseModes(DRAW_MODE);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterModeCanTargetOpponentsCreatureWithAnyKindOfCounter() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);
        harness.addToBattlefield(player1, new ConcordWithTheKami());

        advanceToEndStep();
        chooseModes(COUNTER_MODE);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void losingLastCounterMakesTargetIllegalAndStopsAllModes() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);
        attach(new Pacifism(), creature);
        attach(new Bonesplitter(), creature);
        harness.addToBattlefield(player1, new ConcordWithTheKami());
        GrizzlyBears card = new GrizzlyBears();
        harness.setLibrary(player1, List.of(card));

        advanceToEndStep();
        chooseModes(COUNTER_MODE, DRAW_MODE, SPIRIT_MODE);
        harness.handlePermanentChosen(player1, creature.getId());
        creature.setCounterCount(CounterType.CHARGE, 0);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    void conditionalModesCheckAttachmentsAtResolution() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ConcordWithTheKami());
        GrizzlyBears card = new GrizzlyBears();
        harness.setLibrary(player1, List.of(card));

        advanceToEndStep();
        chooseModes(DRAW_MODE, SPIRIT_MODE);
        attach(new Pacifism(), creature);
        attach(new Bonesplitter(), creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    void opponentsAttachmentsCountButOpponentsCreaturesDoNot() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(ownCreature.getId());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        equipment.setAttachedTo(opposingCreature.getId());
        harness.addToBattlefield(player1, new ConcordWithTheKami());
        GrizzlyBears card = new GrizzlyBears();
        harness.setLibrary(player1, List.of(card));

        advanceToEndStep();
        chooseModes(DRAW_MODE, SPIRIT_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    private void chooseModes(String... modes) {
        for (String mode : modes) {
            harness.handleListChoice(player1, mode);
        }
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice) {
            harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
        }
    }

    private void attach(com.github.laxika.magicalvibes.model.Card card, Permanent creature) {
        Permanent attachment = harness.addToBattlefieldAndReturn(player1, card);
        attachment.setAttachedTo(creature.getId());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
