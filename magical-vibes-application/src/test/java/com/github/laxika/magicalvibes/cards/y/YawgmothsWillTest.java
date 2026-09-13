package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.c.ClawsOfGix;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.h.HeatRay;
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

@CardUsed({YawgmothsWill.class, Forest.class, DarkRitual.class, GoblinRaider.class, HeatRay.class,
        ClawsOfGix.class, Firebolt.class})
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

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
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

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
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

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(will, firebolt);
    }
}
