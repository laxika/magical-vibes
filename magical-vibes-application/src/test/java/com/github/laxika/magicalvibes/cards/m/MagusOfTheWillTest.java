package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({MagusOfTheWill.class, Forest.class, DarkRitual.class, GoblinRaider.class, Shock.class})
class MagusOfTheWillTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles itself and lets its controller play lands and cast spells from the graveyard")
    void playsLandAndCastsSpellFromGraveyard() {
        Permanent magus = addReadyMagus();
        Forest forest = new Forest();
        DarkRitual ritual = new DarkRitual();
        harness.setGraveyard(player1, List.of(forest, ritual));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.playGraveyardLand(player1, 0);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(magus.getCard(), ritual);
    }

    @Test
    @DisplayName("The graveyard replacement expires at the end of the turn")
    void replacementExpiresAtEndOfTurn() {
        Permanent magus = addReadyMagus();
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, raider.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Raider");
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(magus.getCard());
    }

    @Test
    @DisplayName("Exiling Magus is a cost paid before its ability resolves")
    void exilesAsActivationCost() {
        Permanent magus = addReadyMagus();
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Magus of the Will");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(magus.getCard());
        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.playGraveyardLand(player1, 0);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("A summoning-sick Magus cannot pay its tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent magus = addReadyMagus();
        magus.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Magus of the Will");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(magus.getCard());
    }

    @Test
    @DisplayName("A tapped Magus cannot activate")
    void cannotActivateWhileTapped() {
        Permanent magus = addReadyMagus();
        magus.setTapped(true);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Magus of the Will");
    }

    @Test
    @DisplayName("Magus cannot activate without enough mana to pay its cost")
    void cannotActivateWithoutEnoughMana() {
        Permanent magus = addReadyMagus();
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Magus of the Will");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(magus.getCard());
    }

    @Test
    @DisplayName("Graveyard permission does not grant an additional land play")
    void cannotPlaySecondLand() {
        addReadyMagus();
        harness.setHand(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.playLand(player1, 0);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The replacement exiles the controller's dying creatures and hand spells but not opponents' cards")
    void replacementOnlyAppliesToControllersGraveyard() {
        addReadyMagus();
        Permanent ownRaider = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        Permanent opposingRaider = harness.addToBattlefieldAndReturn(player2, new GoblinRaider());
        Shock firstShock = new Shock();
        Shock secondShock = new Shock();
        harness.setHand(player1, List.of(firstShock, secondShock));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, ownRaider.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, opposingRaider.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(ownRaider.getCard(), firstShock, secondShock);
        harness.assertNotInGraveyard(player1, "Goblin Raider");
        harness.assertNotInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Goblin Raider");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(opposingRaider.getCard());
    }

    @Test
    @DisplayName("The graveyard play permission expires at end of turn")
    void playPermissionExpiresAtEndOfTurn() {
        addReadyMagus();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setGraveyard(player1, List.of(new Forest(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Dark Ritual");
    }

    private Permanent addReadyMagus() {
        Permanent magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheWill());
        magus.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return magus;
    }
}
