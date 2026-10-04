package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HyldaOfTheIcyCrown.class, IcyManipulator.class, GrizzlyBears.class})
class HyldaOfTheIcyCrownTest extends BaseCardTest {

    private static final String TOKEN = "Create a 4/4 white and blue Elemental creature token";
    private static final String COUNTERS = "Put a +1/+1 counter on each creature you control";
    private static final String SCRY = "Scry 2, then draw a card";

    @Test
    void payingCreatesAnElementalToken() {
        setupBoard();
        int creaturesBefore = findPermanents(player1, "Grizzly Bears").size();

        triggerHylda();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, TOKEN);
        harness.passBothPriorities();

        List<Permanent> elementals = findPermanents(player1, "Elemental");
        assertThat(elementals).hasSize(1);
        assertThat(elementals.getFirst().getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(elementals.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(elementals.getFirst().getEffectivePower()).isEqualTo(4);
        assertThat(elementals.getFirst().getEffectiveToughness()).isEqualTo(4);
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(creaturesBefore);
    }

    @Test
    void payingPutsCountersOnEachCreatureYouControl() {
        setupBoard();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        triggerHylda();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, COUNTERS);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Hylda of the Icy Crown")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Icy Manipulator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .allMatch(permanent -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 1);
        assertThat(findPermanents(player2, "Grizzly Bears"))
                .allMatch(permanent -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 0);
    }

    @Test
    void payingScriesThenDraws() {
        setupBoard();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        triggerHylda();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, SCRY);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()).getLast()).isSameAs(first);
    }

    @Test
    void decliningPaymentDoesNothing() {
        setupBoard();

        triggerHylda();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Elemental")).isEmpty();
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .allMatch(permanent -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 0);
    }

    @Test
    void tappingAlreadyTappedCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new HyldaOfTheIcyCrown());
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    void paidModeUsesASeparateTriggerBeforeCreatingToken() {
        setupBoard();
        triggerHylda();

        harness.withAutoStop(gd.currentStep, () -> {
            harness.handleMayAbilityChosen(player1, true);
            harness.handleListChoice(player1, TOKEN);

            assertThat(findPermanents(player1, "Elemental")).isEmpty();
            assertThat(gd.stack).hasSize(1);

            harness.passBothPriorities();

            assertThat(findPermanents(player1, "Elemental")).hasSize(1);
        });
    }

    @Test
    void tappingYourOwnCreatureDoesNotTrigger() {
        setupBoard();
        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");

        harness.activateAbility(player1, 1, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tappingOpponentsNoncreatureDoesNotTrigger() {
        setupBoard();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());

        harness.activateAbility(player1, 1, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentTappingTheirOwnCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new HyldaOfTheIcyCrown());
        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupBoard() {
        harness.addToBattlefield(player1, new HyldaOfTheIcyCrown());
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void triggerHylda() {
        Permanent opponentCreature = findPermanents(player2, "Grizzly Bears").getFirst();
        harness.activateAbility(player1, 1, null, opponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
