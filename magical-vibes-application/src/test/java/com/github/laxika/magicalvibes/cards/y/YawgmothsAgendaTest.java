package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.AgonizingDemise;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.z.Zap;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YawgmothsAgenda.class, AgonizingDemise.class, Mountain.class,
        YavimayaBarbarian.class, Zap.class})
class YawgmothsAgendaTest extends BaseCardTest {

    @Test
    @DisplayName("Controller may play a land and cast a spell from their graveyard")
    void playsLandAndCastsSpellFromGraveyard() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setGraveyard(player1, List.of(new Mountain(), new Zap()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Zap"));
    }

    @Test
    @DisplayName("Controller cannot cast a second spell in the same turn")
    void cannotCastSecondSpellThisTurn() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setGraveyard(player1, List.of(new Zap(), new Zap()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cards put into the controller's graveyard are exiled instead")
    void exilesOwnCardsInsteadOfGraveyard() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.addToBattlefield(player1, new YavimayaBarbarian());
        harness.setHand(player1, List.of(new AgonizingDemise()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Yavimaya Barbarian"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Yavimaya Barbarian");
        harness.assertNotInGraveyard(player1, "Yavimaya Barbarian");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Yavimaya Barbarian"))
                .anyMatch(card -> card.getName().equals("Agonizing Demise"));
    }

    @Test
    @DisplayName("The controller can cast a creature spell from their graveyard")
    void castsPermanentSpellFromGraveyard() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setGraveyard(player1, List.of(new YavimayaBarbarian()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Yavimaya Barbarian");
        harness.assertNotInGraveyard(player1, "Yavimaya Barbarian");
    }

    @Test
    @DisplayName("A spell cast from hand also uses the controller's spell for the turn")
    void handSpellCountsTowardSpellLimit() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setHand(player1, List.of(new Zap()));
        harness.setGraveyard(player1, List.of(new Zap()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The graveyard permissions apply only to the Agenda's controller")
    void opponentCannotCastFromTheirGraveyard() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setGraveyard(player2, List.of(new Zap()));
        harness.setHand(player2, List.of());
        harness.addMana(player2, ManaColor.RED, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Cards put into an opponent's graveyard are not exiled")
    void doesNotExileOpponentsCards() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.addToBattlefield(player2, new YavimayaBarbarian());
        harness.setHand(player1, List.of(new AgonizingDemise()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Yavimaya Barbarian"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Yavimaya Barbarian");
        harness.assertInGraveyard(player2, "Yavimaya Barbarian");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Agonizing Demise"));
    }

    @Test
    @DisplayName("Losing Agenda's abilities removes its graveyard permissions")
    void losingAbilitiesRemovesGraveyardPermissions() {
        Permanent agenda = harness.addToBattlefieldAndReturn(player1, new YawgmothsAgenda());
        agenda.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setGraveyard(player1, List.of(new Mountain(), new Zap()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Losing Agenda's abilities removes its graveyard spell permission")
    void losingAbilitiesRemovesGraveyardSpellPermission() {
        Permanent agenda = harness.addToBattlefieldAndReturn(player1, new YawgmothsAgenda());
        agenda.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setGraveyard(player1, List.of(new Zap()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Losing Agenda's abilities removes its spell limit")
    void losingAbilitiesRemovesSpellLimit() {
        Permanent agenda = harness.addToBattlefieldAndReturn(player1, new YawgmothsAgenda());
        agenda.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setHand(player1, List.of(new Zap(), new Zap()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Losing Agenda's abilities removes its graveyard replacement effect")
    void losingAbilitiesRemovesGraveyardReplacement() {
        Permanent agenda = harness.addToBattlefieldAndReturn(player1, new YawgmothsAgenda());
        agenda.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.addToBattlefield(player1, new YavimayaBarbarian());
        harness.setHand(player1, List.of(new AgonizingDemise()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Yavimaya Barbarian"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Yavimaya Barbarian");
        harness.assertInGraveyard(player1, "Agonizing Demise");
    }
}
