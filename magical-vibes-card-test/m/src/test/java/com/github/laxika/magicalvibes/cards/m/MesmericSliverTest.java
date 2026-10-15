package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.v.VirulentSliver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MesmericSliver.class, VirulentSliver.class, MistmeadowSkulk.class})
class MesmericSliverTest extends BaseCardTest {

    @Test
    @DisplayName("A Sliver entering may fateseal an opponent's library")
    void sliverEnteringMayFatesealOpponentLibrary() {
        addCreatureReady(player1, new MesmericSliver());
        Card topCard = new MistmeadowSkulk();
        Card nextCard = new MistmeadowSkulk();
        harness.setLibrary(player2, List.of(topCard, nextCard));

        harness.castFromHand(player1, new VirulentSliver(), "{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.Scry fateseal = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(fateseal.playerId()).isEqualTo(player1.getId());
        assertThat(fateseal.libraryOwnerId()).isEqualTo(player2.getId());
        assertThat(fateseal.causesScryTriggers()).isFalse();
        assertThat(fateseal.cards()).containsExactly(topCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard, topCard);
    }

    @Test
    @DisplayName("Mesmeric Sliver grants the ability to itself")
    void grantsAbilityToItself() {
        Card topCard = new MistmeadowSkulk();
        harness.setLibrary(player2, List.of(topCard));

        harness.castFromHand(player1, new MesmericSliver(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice may =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(may).isNotNull();
        assertThat(may.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ability leaves the opponent's library unchanged")
    void decliningFatesealLeavesLibraryUnchanged() {
        addCreatureReady(player1, new MesmericSliver());
        Card topCard = new MistmeadowSkulk();
        harness.setLibrary(player2, List.of(topCard));

        harness.castFromHand(player1, new VirulentSliver(), "{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Mesmeric Sliver does not grant the ability to non-Slivers")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new MesmericSliver());

        harness.castFromHand(player1, new MistmeadowSkulk(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mistmeadow Skulk");

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Sliver controlled by the opponent also gains the ability")
    void opponentControlledSliverGainsAbility() {
        addCreatureReady(player1, new MesmericSliver());
        Card topCard = new MistmeadowSkulk();
        harness.setLibrary(player1, List.of(topCard));

        harness.enterBattlefieldAndReturn(player2, new VirulentSliver());
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice may =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(may).isNotNull();
        assertThat(may.playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.Scry fateseal = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(fateseal.playerId()).isEqualTo(player2.getId());
        assertThat(fateseal.libraryOwnerId()).isEqualTo(player1.getId());
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Fatesealing an empty opponent library has no follow-up interaction")
    void fatesealingEmptyOpponentLibraryHasNoFollowUpInteraction() {
        addCreatureReady(player1, new MesmericSliver());
        harness.setLibrary(player2, List.of());

        harness.castFromHand(player1, new VirulentSliver(), "{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Two Mesmeric Slivers grant two independent optional fateseals")
    void multipleCopiesGrantIndependentAbilities() {
        addCreatureReady(player1, new MesmericSliver());
        addCreatureReady(player2, new MesmericSliver());
        Card topCard = new MistmeadowSkulk();
        Card nextCard = new MistmeadowSkulk();
        harness.setLibrary(player2, List.of(topCard, nextCard));

        harness.enterBattlefieldAndReturn(player1, new VirulentSliver());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        if (gd.interaction.activeInteraction() == null) {
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.Scry fateseal = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(fateseal.cards()).containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard, topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An already triggered fateseal survives both Slivers leaving")
    void triggeredAbilitySurvivesSourceAndGrantLeaving() {
        Permanent grantingSliver = addCreatureReady(player1, new MesmericSliver());
        Card topCard = new MistmeadowSkulk();
        Card nextCard = new MistmeadowSkulk();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        Permanent enteringSliver = harness.enterBattlefieldAndReturn(player1, new VirulentSliver());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, grantingSliver);
            harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, enteringSliver);
        });
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.Scry fateseal = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(fateseal.playerId()).isEqualTo(player1.getId());
        assertThat(fateseal.cards()).containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
