package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.Gigantoad;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwallowedByLeviathan.class, Gigantoad.class})
class SwallowedByLeviathanTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils before counting the graveyard for the ransom")
    void surveilsBeforeCountingGraveyardForRansom() {
        harness.setGraveyard(player2, List.of(new Gigantoad()));

        Gigantoad target = new Gigantoad();
        harness.addMana(player1, ManaColor.GREEN, 2);

        Card kept = new Gigantoad();
        Card surveilled = new Gigantoad();
        harness.setHand(player2, List.of(new SwallowedByLeviathan()));
        harness.setLibrary(player2, List.of(kept, surveilled));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castFromHand(player1, target, "{3}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(surveilled);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gigantoad");
        harness.assertInGraveyard(player2, "Swallowed by Leviathan");
    }

    @Test
    @DisplayName("Counters the spell when its controller declines the ransom")
    void countersWhenControllerDeclinesRansom() {
        harness.setGraveyard(player2, List.of(new Gigantoad()));

        Gigantoad target = new Gigantoad();
        harness.addMana(player1, ManaColor.GREEN, 3);

        Card surveilledOne = new Gigantoad();
        Card surveilledTwo = new Gigantoad();
        harness.setHand(player2, List.of(new SwallowedByLeviathan()));
        harness.setLibrary(player2, List.of(surveilledOne, surveilledTwo));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castFromHand(player1, target, "{3}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Gigantoad");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(surveilledOne, surveilledTwo);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target only a spell")
    void canTargetOnlySpell() {
        Card permanent = new Gigantoad();
        harness.addToBattlefield(player1, permanent);
        harness.setHand(player2, List.of(new SwallowedByLeviathan()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void keepingBothCardsAllowsZeroPaymentAndReordersLibrary() {
        Gigantoad target = new Gigantoad();
        Card first = new Gigantoad();
        Card second = new Gigantoad();
        harness.setGraveyard(player2, List.of());
        harness.setGraveyard(player1, List.of(new Gigantoad(), new Gigantoad()));
        harness.setLibrary(player2, List.of(first, second));
        harness.castFromHand(player1, target, "{3}{G}");
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new SwallowedByLeviathan()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, first);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gigantoad");
        harness.assertInGraveyard(player2, "Swallowed by Leviathan");
    }

    @Test
    void countersAutomaticallyWhenSurveillingOnlyCardMakesPaymentUnaffordable() {
        Gigantoad target = new Gigantoad();
        Card surveilled = new Gigantoad();
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(surveilled));
        harness.castFromHand(player1, target, "{3}{G}");
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new SwallowedByLeviathan()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        harness.assertInGraveyard(player1, "Gigantoad");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(surveilled);
        harness.assertInGraveyard(player2, "Swallowed by Leviathan");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryStillCountersUsingExistingGraveyard() {
        Gigantoad target = new Gigantoad();
        harness.setGraveyard(player2, List.of(new Gigantoad()));
        harness.setLibrary(player2, List.of());
        harness.castFromHand(player1, target, "{3}{G}");
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new SwallowedByLeviathan()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gigantoad");
        harness.assertInGraveyard(player2, "Swallowed by Leviathan");
        assertThat(gd.stack).isEmpty();
    }
}
