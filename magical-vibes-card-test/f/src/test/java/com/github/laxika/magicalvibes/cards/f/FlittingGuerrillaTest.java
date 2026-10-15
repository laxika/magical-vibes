package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.InvasionOfDominaria;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlittingGuerrilla.class, FurtiveAnalyst.class, InvasionOfDominaria.class, Island.class})
class FlittingGuerrillaTest extends BaseCardTest {

    @Test
    @DisplayName("On death, each player mills two and the accepted exile returns a targeted card to the top of your library")
    void deathTriggerMillsAndExilesForReflexiveReturn() {
        Card target = new FurtiveAnalyst();
        Card invalidTarget = new Island();
        Card guerrilla = new FlittingGuerrilla();
        Permanent permanent = addCreatureReady(player1, guerrilla);
        harness.setGraveyard(player1, List.of(target, invalidTarget));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(target.getId());
        assertThat(choice.validCardIds()).doesNotContain(invalidTarget.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(exiled -> exiled.getId().equals(guerrilla.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(target.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Declining the exile leaves the source and graveyard card untouched")
    void decliningExileDoesNotCreateReflexiveTrigger() {
        Card target = new FurtiveAnalyst();
        Card guerrilla = new FlittingGuerrilla();
        Permanent permanent = addCreatureReady(player1, guerrilla);
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(guerrilla.getId(), target.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(exiled -> exiled.getId().equals(guerrilla.getId()));
    }

    @Test
    @DisplayName("The returned card goes on the ability controller's library")
    void returnsToControllerLibrary() {
        Card target = new FurtiveAnalyst();
        Card guerrilla = new FlittingGuerrilla();
        guerrilla.setOwnerId(player2.getId());
        Permanent permanent = addCreatureReady(player1, guerrilla);
        gd.stolenCreatures.put(permanent.getId(), player2.getId());
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(target.getId());
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId()).isNotEqualTo(target.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(exiled -> exiled.getId().equals(guerrilla.getId()));
    }

    @Test
    @DisplayName("The reflexive ability can target a battle just milled, but not an opponent's creature")
    void returnsNewlyMilledBattleAfterSeparateTriggerResolves() {
        Card target = new InvasionOfDominaria();
        Card opponentCard = new FurtiveAnalyst();
        Card remainingCard = new Island();
        Card guerrilla = new FlittingGuerrilla();
        Permanent permanent = addCreatureReady(player1, guerrilla);
        harness.setLibrary(player1, List.of(target, new Island(), remainingCard));
        harness.setLibrary(player2, List.of(opponentCard, new Island(), new Island()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(guerrilla.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).contains(target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(target.getId(), remainingCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).doesNotContain(target.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId).contains(opponentCard.getId());
    }

    @Test
    @DisplayName("Exiling the source is allowed even when there is no legal return target")
    void canExileWithoutLegalTarget() {
        Card guerrilla = new FlittingGuerrilla();
        Permanent permanent = addCreatureReady(player1, guerrilla);
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId).contains(guerrilla.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the chosen card in response leaves the source exiled and returns nothing")
    void removedTargetIsNotReturned() {
        Card target = new FurtiveAnalyst();
        Card guerrilla = new FlittingGuerrilla();
        Permanent permanent = addCreatureReady(player1, guerrilla);
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> !card.getId().equals(target.getId())).toList());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId).doesNotContain(target.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .contains(guerrilla.getId(), target.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A source removed before its death trigger resolves still mills but cannot create the return trigger")
    void absentSourceStillMillsWithoutReturningCard() {
        Card target = new FurtiveAnalyst();
        Card guerrilla = new FlittingGuerrilla();
        Permanent permanent = addCreatureReady(player1, guerrilla);
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.setGraveyard(player1, List.of(target));
        harness.setExile(player1, List.of(guerrilla));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).contains(target.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId).containsExactly(guerrilla.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
