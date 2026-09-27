package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimeGorger.class, GrizzlyBears.class, Shock.class})
class GrimeGorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking exiles one card of each chosen card type and grows Grime Gorger")
    void exilesOneOfEachChosenCardType() {
        Card creature = new GrizzlyBears();
        Card instant = new Shock();
        Card otherCreature = new GrizzlyBears();
        Card ownInstant = new Shock();
        harness.setGraveyard(player2, List.of(creature, instant, otherCreature));
        harness.setGraveyard(player1, List.of(ownInstant));

        Permanent gorger = addCreatureReady(player1, new GrimeGorger());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(gorger)));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                creature.getId(), instant.getId(), otherCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), instant.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(creature, instant);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(otherCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownInstant);
        assertThat(gorger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two cards with the same card type cannot both be chosen")
    void rejectsDuplicateCardTypes() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(firstCreature, secondCreature));

        Permanent gorger = addCreatureReady(player1, new GrimeGorger());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(gorger)));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Selected cards must have different card types");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
    }
}
