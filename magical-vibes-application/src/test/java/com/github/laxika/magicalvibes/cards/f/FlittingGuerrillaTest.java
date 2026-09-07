package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FlittingGuerrilla.class, GrizzlyBears.class, Island.class})
class FlittingGuerrillaTest extends BaseCardTest {

    @Test
    @DisplayName("On death, each player mills two and the accepted exile returns a targeted card to the top of its owner's library")
    void deathTriggerMillsAndExilesForReflexiveReturn() {
        Card target = new GrizzlyBears();
        Card invalidTarget = new Island();
        Card guerrilla = new FlittingGuerrilla();
        addCreatureReady(player1, guerrilla);
        harness.setGraveyard(player1, List.of(target, invalidTarget));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
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
        Card target = new GrizzlyBears();
        Card guerrilla = new FlittingGuerrilla();
        addCreatureReady(player1, guerrilla);
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
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
        Card target = new GrizzlyBears();
        target.setOwnerId(player2.getId());
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
}
