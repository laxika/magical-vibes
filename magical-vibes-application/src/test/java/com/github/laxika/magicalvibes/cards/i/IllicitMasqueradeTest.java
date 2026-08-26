package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllicitMasquerade.class, GrizzlyBears.class, WrathOfGod.class})
class IllicitMasqueradeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with an impostor counter on each creature the controller controls")
    void entersWithImpostorCountersOnControlledCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castMasquerade();

        assertThat(creature.getCounterCount(CounterType.IMPOSTOR)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.IMPOSTOR)).isZero();
    }

    @Test
    @DisplayName("Exiles the impostor that dies and returns another creature card from the graveyard")
    void exilesDyingImpostorAndReturnsAnotherCreature() {
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        Card creatureToReturn = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(creatureToReturn)));
        castMasquerade();

        destroyAllCreatures();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creatureToReturn.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creatureToReturn.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(dyingCreature.getCard().getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(dyingCreature.getCard().getId(), creatureToReturn.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .contains(creatureToReturn.getId());
    }

    private void castMasquerade() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new IllicitMasquerade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private void destroyAllCreatures() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }
}
