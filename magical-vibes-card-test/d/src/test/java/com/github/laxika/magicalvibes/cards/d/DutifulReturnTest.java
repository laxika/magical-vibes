package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.h.HighlandGame;
import com.github.laxika.magicalvibes.cards.b.BribersPurse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DutifulReturn.class, AlpineGrizzly.class, HighlandGame.class, BribersPurse.class})
class DutifulReturnTest extends BaseCardTest {

    @Test
    void castingWithCreatureCardsPromptsForUpToTwoTargets() {
        harness.setGraveyard(player1, List.of(new AlpineGrizzly(), new HighlandGame()));
        harness.setHand(player1, List.of(new DutifulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice interaction =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(interaction).isNotNull();
        assertThat(interaction.maxCount()).isEqualTo(2);
        assertThat(interaction.validCardIds()).hasSize(2);
    }

    @Test
    void choosingTwoCreatureCardsReturnsBothToHand() {
        Card creature1 = new AlpineGrizzly();
        Card creature2 = new HighlandGame();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new DutifulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Alpine Grizzly");
        harness.assertInHand(player1, "Highland Game");
        harness.assertNotInGraveyard(player1, "Alpine Grizzly");
        harness.assertNotInGraveyard(player1, "Highland Game");
    }

    @Test
    void choosingOneCreatureCardLeavesTheOtherInGraveyard() {
        Card creature = new AlpineGrizzly();
        Card otherCreature = new HighlandGame();
        harness.setGraveyard(player1, List.of(creature, otherCreature));
        harness.setHand(player1, List.of(new DutifulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Alpine Grizzly");
        harness.assertInGraveyard(player1, "Highland Game");
    }

    @Test
    void onlyCreatureCardsAreValidTargets() {
        Card creature = new AlpineGrizzly();
        harness.setGraveyard(player1, List.of(creature, new BribersPurse()));
        harness.setHand(player1, List.of(new DutifulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
    }

    @Test
    void noCreatureCardsSkipTargetPrompt() {
        harness.setGraveyard(player1, List.of(new BribersPurse()));
        harness.setHand(player1, List.of(new DutifulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void choosingZeroTargetsReturnsNoCards() {
        Card creature = new AlpineGrizzly();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DutifulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alpine Grizzly");
        harness.assertNotInHand(player1, "Alpine Grizzly");
        harness.assertInGraveyard(player1, "Dutiful Return");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentGraveyardCardsAreNotOfferedAsTargets() {
        Card ownCreature = new AlpineGrizzly();
        Card opponentCreature = new HighlandGame();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new DutifulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(ownCreature.getId());
    }

    @Test
    void remainingTargetReturnsWhenOtherTargetLeavesGraveyard() {
        Card creature1 = new AlpineGrizzly();
        Card creature2 = new HighlandGame();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new DutifulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature1.getId(), creature2.getId()));
        harness.setGraveyard(player1, List.of(creature2));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Alpine Grizzly");
        harness.assertInHand(player1, "Highland Game");
        harness.assertNotInGraveyard(player1, "Highland Game");
        harness.assertInGraveyard(player1, "Dutiful Return");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noCardsReturnWhenAllTargetsLeaveGraveyard() {
        Card creature1 = new AlpineGrizzly();
        Card creature2 = new HighlandGame();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new DutifulReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature1.getId(), creature2.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Alpine Grizzly");
        harness.assertNotInHand(player1, "Highland Game");
        harness.assertInGraveyard(player1, "Dutiful Return");
        assertThat(gd.stack).isEmpty();
    }
}
