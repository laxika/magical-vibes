package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.AgonizingDemise;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.Recoil;
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
        YavimayaBarbarian.class, Zap.class, Recoil.class, Firebolt.class})
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

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
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

    @Test
    @DisplayName("Casting Agenda itself uses the controller's spell for the turn")
    void agendaItselfCountsTowardSpellLimit() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(new Zap()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFromHand(player1, new YawgmothsAgenda(), "{3}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Yawgmoth's Agenda");
        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The controller may cast one graveyard instant on an opponent's turn")
    void castsInstantOnOpponentsTurn() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setGraveyard(player1, List.of(new Zap(), new Zap()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Agenda does not restrict the opponent to one spell per turn")
    void opponentMayCastMultipleSpells() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setHand(player2, List.of(new Zap(), new Zap()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Zap")).hasSize(2);
    }

    @Test
    @DisplayName("Agenda does not grant an additional land play")
    void graveyardLandUsesNormalLandPlay() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setGraveyard(player1, List.of(new Mountain(), new Mountain()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Mountain")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Creature spells from the graveyard retain their normal timing restrictions")
    void cannotCastCreatureOnOpponentsTurn() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setGraveyard(player1, List.of(new YavimayaBarbarian()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Yavimaya Barbarian");
        harness.assertNotOnBattlefield(player1, "Yavimaya Barbarian");
    }

    @Test
    @DisplayName("Graveyard spells still require their mana costs")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setGraveyard(player1, List.of(new Zap()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Zap");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A graveyard spell goes to the graveyard if Agenda leaves during its resolution")
    void removingAgendaDuringResolutionEndsReplacement() {
        Permanent agenda = harness.addToBattlefieldAndReturn(player1, new YawgmothsAgenda());
        harness.setGraveyard(player1, List.of(new Recoil()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyardTargeting(player1, 0, agenda.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Yawgmoth's Agenda");
        harness.assertInGraveyard(player1, "Yawgmoth's Agenda");
        harness.assertInGraveyard(player1, "Recoil");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Agenda allows a card with flashback to be cast for its normal mana cost")
    void castsFlashbackCardForNormalManaCost() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setGraveyard(player1, List.of(new Firebolt()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotInGraveyard(player1, "Firebolt");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Firebolt"));
    }
}
