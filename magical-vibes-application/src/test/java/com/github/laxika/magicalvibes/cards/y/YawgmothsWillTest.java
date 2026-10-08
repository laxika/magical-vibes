package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.c.ClawsOfGix;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.DriftingMeadow;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.h.HeatRay;
import com.github.laxika.magicalvibes.cards.l.LunarchVeteran;
import com.github.laxika.magicalvibes.cards.w.Whetstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YawgmothsWill.class, Forest.class, DarkRitual.class, GoblinRaider.class, HeatRay.class,
        ClawsOfGix.class, Firebolt.class, LunarchVeteran.class, DriftingMeadow.class, Whetstone.class})
class YawgmothsWillTest extends BaseCardTest {

    @Test
    @DisplayName("Plays a land and casts a spell from the graveyard this turn")
    void playsLandAndCastsSpellFromGraveyard() {
        YawgmothsWill will = new YawgmothsWill();
        Forest forest = new Forest();
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(will));
        harness.setGraveyard(player1, List.of(forest, ritual));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.playGraveyardLand(player1, 0);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(will, ritual);
    }

    @Test
    @DisplayName("Exiles own cards that would enter the graveyard this turn")
    void exilesOwnCardsInsteadOfGraveyard() {
        YawgmothsWill will = new YawgmothsWill();
        GoblinRaider raider = new GoblinRaider();
        HeatRay heatRay = new HeatRay();
        harness.setHand(player1, List.of(will, heatRay));
        harness.addToBattlefield(player1, raider);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.castInstant(player1, 0, 2, harness.getPermanentId(player1, "Goblin Raider"));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(will, heatRay, raider);
    }

    @Test
    @DisplayName("The graveyard replacement expires at the end of the turn")
    void replacementExpiresAtEndOfTurn() {
        YawgmothsWill will = new YawgmothsWill();
        GoblinRaider raider = new GoblinRaider();
        HeatRay heatRay = new HeatRay();
        harness.setHand(player1, List.of(will, heatRay));
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, raider);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player1, "Goblin Raider"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Heat Ray");
        harness.assertInGraveyard(player1, "Goblin Raider");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(will).doesNotContain(heatRay, raider);
    }

    @Test
    @DisplayName("Exiles a permanent sacrificed as an ability cost")
    void exilesPermanentSacrificedAsAbilityCost() {
        YawgmothsWill will = new YawgmothsWill();
        ClawsOfGix claws = new ClawsOfGix();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(will));
        harness.addToBattlefield(player1, claws);
        Permanent forestPermanent = harness.addToBattlefieldAndReturn(player1, forest);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forestPermanent.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertOnBattlefield(player1, "Claws of Gix");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(will, forest);
    }

    @Test
    @DisplayName("Can cast a card with flashback for its normal mana cost")
    void castsFlashbackCardForNormalManaCost() {
        YawgmothsWill will = new YawgmothsWill();
        Firebolt firebolt = new Firebolt();
        harness.setHand(player1, List.of(will));
        harness.setGraveyard(player1, List.of(firebolt));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(will, firebolt);
    }

    @Test
    @DisplayName("Casts a creature from the graveyard for its normal cost")
    void castsCreatureForNormalCost() {
        GoblinRaider raider = new GoblinRaider();
        harness.setHand(player1, List.of(new YawgmothsWill()));
        harness.setGraveyard(player1, List.of(raider));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Raider");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(raider);
    }

    @Test
    @DisplayName("Can cast a disturb card untransformed for its normal cost")
    void castsDisturbCardForNormalCost() {
        harness.setHand(player1, List.of(new YawgmothsWill()));
        harness.setGraveyard(player1, List.of(new LunarchVeteran()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lunarch Veteran");
        harness.assertNotOnBattlefield(player1, "Luminous Phantom");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not allow a second land play this turn")
    void respectsLandPlayLimit() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of(new YawgmothsWill()));
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.playGraveyardLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Does not waive mana costs for graveyard spells")
    void requiresManaForGraveyardSpells() {
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(new YawgmothsWill()));
        harness.setGraveyard(player1, List.of(ritual));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ritual);
    }

    @Test
    @DisplayName("Does not grant instant timing to creature spells")
    void respectsCreatureSpellTiming() {
        GoblinRaider raider = new GoblinRaider();
        harness.setHand(player1, List.of(new YawgmothsWill()));
        harness.setGraveyard(player1, List.of(raider));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(raider);
    }

    @Test
    @DisplayName("Does not exile an opponent's cards")
    void doesNotExileOpponentsCards() {
        GoblinRaider raider = new GoblinRaider();
        harness.setHand(player1, List.of(new YawgmothsWill(), new HeatRay()));
        harness.addToBattlefield(player2, raider);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.castInstant(player1, 0, 2, harness.getPermanentId(player2, "Goblin Raider"));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(raider);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(raider);
        harness.assertNotOnBattlefield(player2, "Goblin Raider");
    }

    @Test
    @DisplayName("Graveyard spell and land permissions expire at end of turn")
    void playPermissionsExpireAtEndOfTurn() {
        DarkRitual ritual = new DarkRitual();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new YawgmothsWill()));
        harness.setHand(player2, List.of());
        harness.setGraveyard(player1, List.of(ritual, forest));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ritual, forest);
    }

    @Test
    @DisplayName("Exiles cards discarded for cycling without preventing the draw")
    void exilesDiscardedCard() {
        YawgmothsWill will = new YawgmothsWill();
        DriftingMeadow meadow = new DriftingMeadow();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(will, meadow));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(will, meadow);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Exiles milled cards only for the player who resolved Will")
    void exilesMilledCards() {
        YawgmothsWill will = new YawgmothsWill();
        Forest ownLand = new Forest();
        DarkRitual ownSpell = new DarkRitual();
        Forest opposingLand = new Forest();
        DarkRitual opposingSpell = new DarkRitual();
        harness.setHand(player1, List.of(will));
        harness.setLibrary(player1, List.of(ownLand, ownSpell));
        harness.setLibrary(player2, List.of(opposingLand, opposingSpell));
        harness.addToBattlefield(player1, new Whetstone());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(will, ownLand, ownSpell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingLand, opposingSpell);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(opposingLand, opposingSpell);
    }
}
