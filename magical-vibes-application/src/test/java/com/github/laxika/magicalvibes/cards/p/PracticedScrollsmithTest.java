package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PracticedScrollsmithTest extends BaseCardTest {

    private void castScrollsmith() {
        harness.setHand(player1, List.of(new PracticedScrollsmith()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice
    }

    // ===== ETB targeting =====

    @Test
    @DisplayName("ETB with a noncreature nonland card in graveyard prompts graveyard choice")
    void etbPromptsGraveyardChoice() {
        harness.setGraveyard(player1, List.of(new Shock()));
        castScrollsmith();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("ETB only offers noncreature, nonland cards from your graveyard")
    void etbOnlyOffersNoncreatureNonland() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock, new GrizzlyBears(), new Island()));
        castScrollsmith();

        List<UUID> validIds = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(validIds).containsExactly(shock.getId());
    }

    @Test
    @DisplayName("ETB does not offer cards from opponent's graveyard")
    void etbDoesNotOfferOpponentGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new Shock()));
        castScrollsmith();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB with no valid target does not prompt")
    void etbNoValidTargetDoesNotPrompt() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Island()));
        castScrollsmith();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Exiles the chosen card and grants its controller play permission")
    void exilesAndGrantsPlayPermission() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castScrollsmith();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
        assertThat(gd.exilePlayPermissions.get(shock.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKey(shock.getId());
    }

    @Test
    @DisplayName("ETB fizzles when the chosen card leaves the graveyard before resolution")
    void fizzlesWhenTargetRemoved() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castScrollsmith();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(shock.getId());
    }
}
