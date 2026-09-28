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

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
