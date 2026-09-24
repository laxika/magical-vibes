package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({GaeasWill.class, Forest.class, DarkRitual.class, GoblinRaider.class, Shock.class})
class GaeasWillTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Gaea's Will with four time counters")
    void suspendExilesWithFourTimeCounters() {
        GaeasWill card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    @DisplayName("The suspended spell lets its controller play lands and cast spells from the graveyard")
    void suspendedSpellAllowsGraveyardPlayAndCast() {
        GaeasWill card = suspendCard();
        Forest forest = new Forest();
        DarkRitual ritual = new DarkRitual();
        harness.setGraveyard(player1, List.of(forest, ritual));

        resolveSuspendedCard();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.playGraveyardLand(player1, 0);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card, ritual);
    }

    @Test
    @DisplayName("The graveyard replacement expires at the end of the turn")
    void graveyardReplacementExpiresAtEndOfTurn() {
        suspendCard();
        harness.setHand(player2, List.of());
        resolveSuspendedCard();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock, creature.getCard());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        Permanent nextCreature = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        Shock nextShock = new Shock();
        harness.setHand(player1, List.of(nextShock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, nextCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nextShock, nextCreature.getCard());
    }

    private GaeasWill suspendCard() {
        GaeasWill card = new GaeasWill();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void resolveSuspendedCard() {
        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }
}
