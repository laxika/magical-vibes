package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalothNull.class, Forest.class, GrizzlyBears.class})
class BalothNullTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts to return up to two creature cards from the graveyard")
    void etbPromptsForCreatures() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setHand(player1, List.of(new BalothNull()));

        castBalothNull();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstCreature.getId(), secondCreature.getId());
    }

    @Test
    @DisplayName("Returning two creature cards puts both in hand")
    void returnsTwoCreaturesToHand() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        Card nonCreature = new Forest();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature, nonCreature));
        harness.setHand(player1, List.of(new BalothNull()));

        castBalothNull();

        List<UUID> targets = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, targets);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCreature, secondCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonCreature);
    }

    @Test
    @DisplayName("Only creature cards are valid targets")
    void onlyCreaturesAreValidTargets() {
        Card nonCreature = new Forest();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonCreature, creature));
        harness.setHand(player1, List.of(new BalothNull()));

        castBalothNull();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Choosing zero targets returns nothing")
    void choosingZeroReturnsNothing() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BalothNull()));

        castBalothNull();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("No creature cards in the graveyard creates no prompt")
    void noCreaturesNoPrompt() {
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.setHand(player1, List.of(new BalothNull()));

        castBalothNull();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
    }

    private void castBalothNull() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
