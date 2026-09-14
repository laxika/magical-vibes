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
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, raider.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Raider");
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(magus.getCard());
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
