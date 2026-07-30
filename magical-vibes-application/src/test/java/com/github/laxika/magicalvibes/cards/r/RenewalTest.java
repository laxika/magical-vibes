package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RenewalTest extends BaseCardTest {

    private UUID castRenewalSacrificingLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Renewal()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        GameData gd = harness.getGameData();
        UUID landId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        harness.castSorceryWithSacrifice(player1, 0, landId);
        return landId;
    }

    @Test
    @DisplayName("Casting sacrifices a land as an additional cost")
    void castingSacrificesLand() {
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));

        castRenewalSacrificingLand();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving offers only basic land cards from the library")
    void resolvingOffersOnlyBasicLands() {
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));

        castRenewalSacrificingLand();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().map(Card::getName)).containsExactly("Plains");
    }

    @Test
    @DisplayName("Choosing a basic land puts it onto the battlefield untapped and schedules a draw")
    void chosenLandEntersUntappedAndDrawIsScheduled() {
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));

        castRenewalSacrificingLand();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().getName()).isEqualTo("Plains");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));

        castRenewalSacrificingLand();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }
}
