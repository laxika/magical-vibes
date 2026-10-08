package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WhitesunsPassage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeinwitchCoven.class, GrizzlyBears.class, WhitesunsPassage.class})
class VeinwitchCovenTest extends BaseCardTest {

    @Test
    void payingBlackReturnsTargetCreatureFromOwnGraveyard() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new VeinwitchCoven());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WhitesunsPassage()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void decliningPaymentLeavesTargetCreatureInGraveyard() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new VeinwitchCoven());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WhitesunsPassage()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    void onlyOwnCreatureCardsAreValidTargets() {
        Card noncreature = new WhitesunsPassage();
        Card opposingCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, new VeinwitchCoven());
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(new WhitesunsPassage()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
        harness.assertInGraveyard(player1, "Whitesun's Passage");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void gainingFiveLifeCreatesOnlyOneReturnTrigger() {
        Card target = new VeinwitchCoven();
        Card otherCreature = new VeinwitchCoven();
        harness.addToBattlefield(player1, new VeinwitchCoven());
        harness.setGraveyard(player1, List.of(target, otherCreature));
        harness.setHand(player1, List.of(new WhitesunsPassage()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCreature).doesNotContain(target);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsLifeGainDoesNotTriggerCoven() {
        Card target = new VeinwitchCoven();
        harness.addToBattlefield(player1, new VeinwitchCoven());
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player2, List.of(new WhitesunsPassage()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0);

        harness.assertLife(player2, 25);
        harness.assertInGraveyard(player1, "Veinwitch Coven");
        harness.assertNotInHand(player1, "Veinwitch Coven");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void targetLeavingGraveyardBeforeResolutionPreventsPaymentPrompt() {
        Card target = new VeinwitchCoven();
        harness.addToBattlefield(player1, new VeinwitchCoven());
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new WhitesunsPassage()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Veinwitch Coven");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getId()));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
