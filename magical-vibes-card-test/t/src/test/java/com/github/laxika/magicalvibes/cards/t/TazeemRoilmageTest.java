package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FeedTheSwarm;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TazeemRoilmage.class, IntoTheRoil.class, FeedTheSwarm.class})
class TazeemRoilmageTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotReturnASpell() {
        Card spell = new IntoTheRoil();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(new TazeemRoilmage()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tazeem Roilmage");
        harness.assertInGraveyard(player1, "Into the Roil");
    }

    @Test
    void kickedReturnsAnInstantOrSorceryFromTheGraveyard() {
        Card instant = new IntoTheRoil();
        Card sorcery = new FeedTheSwarm();
        harness.setGraveyard(player1, List.of(instant, sorcery));
        harness.setHand(player1, List.of(new TazeemRoilmage()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(instant.getId(), sorcery.getId());

        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Feed the Swarm");
        harness.assertNotInGraveyard(player1, "Feed the Swarm");
        harness.assertInGraveyard(player1, "Into the Roil");
    }

    @Test
    void kickedCannotReturnANonSpellCard() {
        Card creature = new TazeemRoilmage();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new TazeemRoilmage()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Tazeem Roilmage");
        harness.assertOnBattlefield(player1, "Tazeem Roilmage");
    }

    @Test
    void kickedReturnsAnInstantAndExcludesCreaturesAndOpponentsCards() {
        Card instant = new IntoTheRoil();
        Card creature = new TazeemRoilmage();
        Card opposingSpell = new FeedTheSwarm();
        harness.setGraveyard(player1, List.of(instant, creature));
        harness.setGraveyard(player2, List.of(opposingSpell));
        harness.setHand(player1, List.of(new TazeemRoilmage()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(instant.getId());
        harness.assertNotInHand(player1, "Into the Roil");

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Into the Roil");
        harness.assertNotInGraveyard(player1, "Into the Roil");
        harness.assertInGraveyard(player1, "Tazeem Roilmage");
        harness.assertInGraveyard(player2, "Feed the Swarm");
    }

    @Test
    void kickedCanEnterWithAnEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new TazeemRoilmage()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tazeem Roilmage");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotChooseAnotherCardWhenTheTargetLeavesTheGraveyard() {
        Card target = new IntoTheRoil();
        Card otherSpell = new FeedTheSwarm();
        harness.setGraveyard(player1, List.of(target, otherSpell));
        harness.setHand(player1, List.of(new TazeemRoilmage()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherSpell));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Into the Roil");
        harness.assertNotInHand(player1, "Feed the Swarm");
        harness.assertInGraveyard(player1, "Feed the Swarm");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedTriggerResolvesAfterRoilmageLeavesTheBattlefield() {
        Card target = new FeedTheSwarm();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new TazeemRoilmage(), new IntoTheRoil()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        addBaseMana();
        harness.castAndResolveInstant(player1, 0,
                gd.playerBattlefields.get(player1.getId()).getFirst().getId());
        harness.assertInHand(player1, "Tazeem Roilmage");
        harness.assertNotOnBattlefield(player1, "Tazeem Roilmage");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Feed the Swarm");
        harness.assertNotInGraveyard(player1, "Feed the Swarm");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
