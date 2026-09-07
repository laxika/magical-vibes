package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BadlandsRevival.class, Forest.class, GrizzlyBears.class})
class BadlandsRevivalTest extends BaseCardTest {

    @Test
    void returnsACreatureToTheBattlefieldAndAPermanentToHand() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        prepareSpell(List.of(creature, land));

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void targetGroupsCanBeDeclinedIndependently() {
        Card creature = new GrizzlyBears();
        prepareSpell(List.of(creature));

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of());
        PendingInteraction.MultiGraveyardChoice handChoice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(handChoice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void choosingTheSameCardForBothGroupsOnlyReturnsItToTheBattlefield() {
        Card creature = new GrizzlyBears();
        prepareSpell(List.of(creature));

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(creature.getId()));
    }

    private void prepareSpell(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new BadlandsRevival()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void castAndBeginTargeting() {
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
    }
}
