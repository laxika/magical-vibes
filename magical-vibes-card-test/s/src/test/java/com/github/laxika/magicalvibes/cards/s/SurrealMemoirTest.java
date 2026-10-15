package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HeatRay;
import com.github.laxika.magicalvibes.cards.o.OgreSentry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurrealMemoir.class, Staggershock.class, OgreSentry.class, HeatRay.class})
class SurrealMemoirTest extends BaseCardTest {

    @Test
    void returnsAnInstantAtRandomAndExilesForRebound() {
        SurrealMemoir card = new SurrealMemoir();
        harness.setGraveyard(player1, List.of(new Staggershock(), new OgreSentry()));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInHand(player1, "Staggershock");
        harness.assertNotInHand(player1, "Ogre Sentry");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundMayCastSurrealMemoirAtNextUpkeepWithoutPayingMana() {
        SurrealMemoir card = new SurrealMemoir();
        harness.setGraveyard(player1, List.of(new Staggershock()));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Surreal Memoir");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void returnsExactlyOneOfMultipleInstantsWithoutAChoice() {
        Staggershock first = new Staggershock();
        HeatRay second = new HeatRay();
        OgreSentry creature = new OgreSentry();
        harness.setGraveyard(player1, List.of(first, second, creature));
        harness.setHand(player1, List.of(new SurrealMemoir()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isIn(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(gd.playerHands.get(player1.getId()).getFirst());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithAnEmptyGraveyardAndStillRebounds() {
        SurrealMemoir card = new SurrealMemoir();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void doesNotReturnNonInstantsOrOpponentsInstants() {
        OgreSentry creature = new OgreSentry();
        SurrealMemoir sorcery = new SurrealMemoir();
        Staggershock opponentsInstant = new Staggershock();
        harness.setGraveyard(player1, List.of(creature, sorcery));
        harness.setGraveyard(player2, List.of(opponentsInstant));
        harness.setHand(player1, List.of(new SurrealMemoir()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, sorcery);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsInstant);
    }

    @Test
    void reboundReturnsAnInstantThatEnteredTheGraveyardAfterTheFirstResolution() {
        SurrealMemoir card = new SurrealMemoir();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        HeatRay instant = new HeatRay();
        harness.setGraveyard(player1, List.of(instant));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
        harness.assertInGraveyard(player1, "Surreal Memoir");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void decliningReboundLeavesTheCardExiledAndDoesNotReturnAnotherInstant() {
        SurrealMemoir card = new SurrealMemoir();
        Staggershock instant = new Staggershock();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.setGraveyard(player1, List.of(instant));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }
}
