package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FightOn.class, GrizzlyBears.class, LlanowarElves.class, LeoninScimitar.class})
class FightOnTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two target creature cards from your graveyard to your hand")
    void returnsUpToTwoCreatureCardsToHand() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Card nonCreature = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(first, second, nonCreature));
        harness.setHand(player1, List.of(new FightOn()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(nonCreature.getId())
                .doesNotContain(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Allows choosing fewer than two creature cards")
    void allowsChoosingFewerThanTwoCards() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new FightOn()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getId());
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new FightOn()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
    }

    @Test
    @DisplayName("Returns only the single selected creature when two are available")
    void returnsOnlySelectedCreature() {
        Card selected = new GrizzlyBears();
        Card unselected = new LlanowarElves();
        harness.setGraveyard(player1, List.of(selected, unselected));
        harness.setHand(player1, List.of(new FightOn()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotInHand(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Can be cast with an empty graveyard")
    void canBeCastWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new FightOn()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Fight On!");
    }

    @Test
    @DisplayName("Returns the remaining target when the other leaves the graveyard")
    void returnsRemainingLegalTarget() {
        Card remaining = new GrizzlyBears();
        Card exiled = new LlanowarElves();
        harness.setGraveyard(player1, List.of(remaining, exiled));
        harness.setHand(player1, List.of(new FightOn()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(remaining.getId(), exiled.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.setExile(player1, List.of(exiled));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Llanowar Elves");
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(exiled);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not return any cards when all chosen targets leave the graveyard")
    void doesNotReturnCardsWhenAllTargetsAreIllegal() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new FightOn()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(first, second));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Fight On!");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rejects choosing more than two creature cards")
    void rejectsMoreThanTwoTargets() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Card third = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new FightOn()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(third);
    }
}
