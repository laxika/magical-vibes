package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.ExileToOwnerGraveyardAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrinningTotemTest extends BaseCardTest {

    private StepTriggerService stepTriggerService() {
        return GameTestEngineContext.get().getBean(StepTriggerService.class);
    }

    private void activateGrinningTotem() {
        harness.addToBattlefield(player1, new GrinningTotem());
        Permanent totem = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grinning Totem"))
                .findFirst().orElseThrow();
        totem.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Chosen card is exiled under the caster with play permission and cleanup scheduled")
    void exilesChosenCardWithPlayPermissionAndSchedulesCleanup() {
        Card swamp = new Swamp();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(swamp);

        activateGrinningTotem();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Card is exiled under the caster's zone, face down, with play permission.
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(swamp.getId()));
        assertThat(gd.findExiledCard(swamp.getId()).faceDown()).isTrue();
        assertThat(gd.exilePlayPermissions.get(swamp.getId())).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(swamp.getId()));

        // Cleanup is scheduled for the caster's next upkeep, targeting the true owner's graveyard.
        List<ExileToOwnerGraveyardAtNextUpkeep> scheduled =
                gd.getDelayedActions(ExileToOwnerGraveyardAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().ownerId()).isEqualTo(player2.getId());
        assertThat(scheduled.getFirst().cardId()).isEqualTo(swamp.getId());

        // Grinning Totem was sacrificed as a cost.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grinning Totem"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grinning Totem"));
    }

    @Test
    @DisplayName("Unplayed exiled card is put into its owner's graveyard at the caster's next upkeep")
    void unplayedCardGoesToOwnerGraveyardAtCasterUpkeep() {
        Card swamp = new Swamp();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(swamp);

        activateGrinningTotem();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Caster's next upkeep.
        gd.activePlayerId = player1.getId();
        stepTriggerService().handleUpkeepTriggers(gd);

        // Card leaves exile, loses permission, and enters its owner's (player2's) graveyard.
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(swamp.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(swamp.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(swamp.getId()));
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cleanup does not fire on an opponent's upkeep — permission lasts until the caster's upkeep")
    void cleanupDoesNotFireOnOpponentUpkeep() {
        Card swamp = new Swamp();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(swamp);

        activateGrinningTotem();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Opponent's upkeep — not "your next upkeep".
        gd.activePlayerId = player2.getId();
        stepTriggerService().handleUpkeepTriggers(gd);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(swamp.getId()));
        assertThat(gd.exilePlayPermissions.get(swamp.getId())).isEqualTo(player1.getId());
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("A card that was played is not put into the graveyard at the caster's upkeep")
    void playedCardIsNotPutIntoGraveyard() {
        Card swamp = new Swamp();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(swamp);

        activateGrinningTotem();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Caster plays the exiled land.
        gs.playCardFromExile(gd, player1, swamp.getId(), null, null);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Swamp"));

        // The caster's upkeep cleanup finds nothing to move — the card stays on the battlefield.
        gd.activePlayerId = player1.getId();
        stepTriggerService().handleUpkeepTriggers(gd);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Swamp"));
        assertThat(gd.playerGraveyards.getOrDefault(player2.getId(), List.of()))
                .noneMatch(c -> c.getId().equals(swamp.getId()));
    }
}
