package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.ShredMemory;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FromTheRubble.class, GrizzlyBears.class, GoblinPiker.class,
        DoublingSeason.class, Naturalize.class, Shock.class, ShredMemory.class})
class FromTheRubbleTest extends BaseCardTest {

    @Test
    void choosesCreatureTypeWhenItEnters() {
        harness.castFromHand(player1, new FromTheRubble(), "{4}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());

        Permanent rubble = findPermanent(player1, "From the Rubble");
        assertThat(rubble.getChosenSubtype()).isEqualTo(CardSubtype.BEAR);
    }

    @Test
    void returnsChosenCreatureTypeWithFinalityCounterAtEndStep() {
        harness.castFromHand(player1, new FromTheRubble(), "{4}{W}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());

        Card bear = new GrizzlyBears();
        Card goblin = new GoblinPiker();
        harness.setGraveyard(player1, List.of(goblin, bear));

        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Goblin Piker");
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        castRubbleChoosingBear();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToEndStep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotTargetMatchingCreaturesInOpponentsGraveyard() {
        castRubbleChoosingBear();
        harness.setGraveyard(player1, List.of(new GoblinPiker()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        advanceToEndStep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Goblin Piker");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void returnsOnlyOneMatchingCreature() {
        castRubbleChoosingBear();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(first.getId()))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
    }

    @Test
    void resolvesAfterRubbleIsDestroyedInResponse() {
        castRubbleChoosingBear();
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "From the Rubble"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "From the Rubble");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.FINALITY))
                .isEqualTo(1);
    }

    @Test
    void finalityExilesReturnedCreatureWhenItWouldDie() {
        castRubbleChoosingBear();
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bear);
    }

    @Test
    void doublingSeasonDoublesFinalityCounterOnReturnedCreature() {
        harness.addToBattlefield(player1, new DoublingSeason());
        castRubbleChoosingBear();
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.FINALITY))
                .isEqualTo(2);
    }

    @Test
    void doesNotChooseAnotherCreatureWhenTargetIsExiledInResponse() {
        castRubbleChoosingBear();
        Card target = new GrizzlyBears();
        Card other = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, other));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setHand(player2, List.of(new ShredMemory()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0);
        harness.handleMultipleCardsChosen(player2, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotPromptForTargetWhenGraveyardIsEmpty() {
        castRubbleChoosingBear();
        harness.setGraveyard(player1, List.of());

        advanceToEndStep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removedFinalityCounterNoLongerExilesCreatureWhenItWouldDie() {
        castRubbleChoosingBear();
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        returned.setCounterCount(CounterType.FINALITY, 0);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, returned.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bear);
    }

    private void castRubbleChoosingBear() {
        harness.castFromHand(player1, new FromTheRubble(), "{4}{W}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
