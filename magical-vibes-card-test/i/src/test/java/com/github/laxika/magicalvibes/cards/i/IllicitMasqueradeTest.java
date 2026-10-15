package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
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

@CardUsed({IllicitMasquerade.class, GrizzlyBears.class, WrathOfGod.class, Opalescence.class})
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

    @Test
    @DisplayName("Exiles the dying impostor even when there is no other creature card to return")
    void exilesImpostorWithoutAnotherCreatureCard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castMasquerade();

        destroyAllCreatures();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(creature.getCard().getId());
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returning a creature is optional, but exiling the dying impostor is mandatory")
    void decliningReturnStillExilesImpostor() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Card otherCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(otherCreature));
        castMasquerade();

        destroyAllCreatures();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(creature.getCard().getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).containsExactly(otherCreature.getId());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creatures entering after the counter ability resolves do not trigger Masquerade when they die")
    void laterCreatureDoesNotTrigger() {
        castMasquerade();
        Card creature = new GrizzlyBears();
        addCreatureReady(player1, creature);
        Card otherCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(otherCreature));

        destroyAllCreatures();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(creature.getId(), otherCreature.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Flash allows Masquerade to be cast during the opponent's turn")
    void canCastDuringOpponentsTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new IllicitMasquerade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passPriority(player2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Illicit Masquerade");
        assertThat(creature.getCounterCount(CounterType.IMPOSTOR)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opposing creature with an impostor counter does not trigger your Masquerade")
    void opposingImpostorDoesNotTrigger() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.IMPOSTOR, 1);
        castMasquerade();

        destroyAllCreatures();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An animated Masquerade with an impostor counter triggers for its own death")
    void animatedMasqueradeTriggersForItsOwnDeath() {
        harness.addToBattlefield(player1, new Opalescence());
        Card creatureToReturn = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creatureToReturn));
        castMasquerade();
        Permanent masquerade = findPermanent(player1, "Illicit Masquerade");
        assertThat(masquerade.getCounterCount(CounterType.IMPOSTOR)).isEqualTo(1);

        destroyAllCreatures();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creatureToReturn.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creatureToReturn.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(masquerade.getCard().getId());
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Illicit Masquerade");
    }

    private void castMasquerade() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new IllicitMasquerade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyAllCreatures() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, (UUID) null);
    }
}
