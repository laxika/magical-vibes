package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({AegisSculptor.class, GrizzlyBears.class, LightningBolt.class, Shock.class})
class AegisSculptorTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting exiles two graveyard cards and puts a +1/+1 counter on Aegis Sculptor")
    void acceptingExilesTwoCardsAndPutsCounterOnSelf() {
        Permanent sculptor = addCreatureReady(player1, new AegisSculptor());
        Card first = new GrizzlyBears();
        Card second = new LightningBolt();
        Card third = new Shock();
        harness.setGraveyard(player1, List.of(first, second, third));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.exileRemainingCount()).isEqualTo(2);

        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(sculptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("Declining the upkeep choice leaves the graveyard and creature unchanged")
    void decliningChoiceDoesNothing() {
        Permanent sculptor = addCreatureReady(player1, new AegisSculptor());
        Card first = new GrizzlyBears();
        Card second = new LightningBolt();
        harness.setGraveyard(player1, List.of(first, second));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sculptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger resolves without a choice when fewer than two cards remain")
    void fewerThanTwoGraveyardCardsCannotPayTheChoice() {
        Permanent sculptor = addCreatureReady(player1, new AegisSculptor());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(sculptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
