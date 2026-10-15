package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelestineTheLivingSaint.class, GrizzlyBears.class, HillGiant.class, Memnite.class})
class CelestineTheLivingSaintTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature whose mana value is at most the life gained this turn")
    void returnsCreatureWithinLifeGainedLimit() {
        harness.addToBattlefield(player1, new CelestineTheLivingSaint());
        Card legal = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(legal, tooExpensive));
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(legal.getId());

        harness.handleMultipleCardsChosen(player1, List.of(legal.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Can return a mana value zero creature even when no life was gained")
    void returnsManaValueZeroCreatureWithoutLifeGain() {
        harness.addToBattlefield(player1, new CelestineTheLivingSaint());
        Memnite memnite = new Memnite();
        harness.setGraveyard(player1, List.of(memnite));

        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(memnite.getId());

        harness.handleMultipleCardsChosen(player1, List.of(memnite.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Memnite");
    }

    @Test
    @DisplayName("Does not offer a creature whose mana value exceeds life gained")
    void doesNotOfferCreatureAboveLifeGainedLimit() {
        harness.addToBattlefield(player1, new CelestineTheLivingSaint());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Healing Tears resolves even if Celestine leaves the battlefield in response")
    void returnsCreatureAfterCelestineLeavesBattlefield() {
        harness.addToBattlefield(player1, new CelestineTheLivingSaint());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        assertThat(gd.stack).hasSize(1);

        harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, gd.playerBattlefields.get(player1.getId()).getFirst());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Celestine, the Living Saint");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new CelestineTheLivingSaint());
        harness.setGraveyard(player1, List.of(new Memnite()));

        advanceToEndStep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Memnite");
    }

    @Test
    @DisplayName("Only offers cards in the controller's graveyard")
    void excludesOpponentsGraveyard() {
        harness.addToBattlefield(player1, new CelestineTheLivingSaint());
        Card ownCard = new GrizzlyBears();
        Card opponentsCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentsCard));
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counts all life gained before Celestine entered, regardless of life lost")
    void countsTotalLifeGainedRatherThanNetLifeChange() {
        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2);
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2);
            harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 6, "test");
        });
        harness.addToBattlefield(player1, new CelestineTheLivingSaint());
        Card target = new HillGiant();
        harness.setGraveyard(player1, List.of(target));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Does not return a target that left the graveyard before resolution")
    void doesNotReturnTargetThatLeftGraveyard() {
        harness.addToBattlefield(player1, new CelestineTheLivingSaint());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(target));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
