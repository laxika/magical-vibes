package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PracticedScrollsmith.class, Shock.class, GrizzlyBears.class, Island.class})
class PracticedScrollsmithTest extends BaseCardTest {

    private void castScrollsmith() {
        harness.castFromHand(player1, new PracticedScrollsmith(), "{R}{R}{W}{W}");
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

    @Test
    void castsExiledCardForItsNormalManaCostAfterSourceLeaves() {
        Shock shock = exileShock();
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerManaPools.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
    }

    @Test
    void permissionSurvivesOpponentsTurnAndExpiresAfterYourNextTurn() {
        Shock shock = exileShock();
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions.get(shock.getId())).isEqualTo(player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions.get(shock.getId())).isEqualTo(player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(shock.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canCastExiledInstantDuringOpponentsTurn() {
        Shock shock = exileShock();
        harness.setLibrary(player2, List.of(new Island()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
    }

    @Test
    void canCastExiledCardAtEndOfYourNextTurn() {
        Shock shock = exileShock();
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
    }

    @Test
    void triggerStillExilesAndGrantsPermissionWhenSourceLeavesBeforeResolution() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castScrollsmith();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
    }

    @Test
    void changingSourceControllerDoesNotInvalidateOriginalControllersGraveyardTarget() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castScrollsmith();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        var scrollsmith = findPermanent(player1, "Practiced Scrollsmith");
        gd.playerBattlefields.get(player1.getId()).remove(scrollsmith);
        gd.playerBattlefields.get(player2.getId()).add(scrollsmith);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
        assertThat(gd.exilePlayPermissions.get(shock.getId())).isEqualTo(player1.getId());
    }

    @Test
    void firstStrikeKillsBlockerBeforeItDealsDamage() {
        addCreatureReady(player1, new PracticedScrollsmith());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Practiced Scrollsmith");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    private Shock exileShock() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castScrollsmith();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        return shock;
    }
}
