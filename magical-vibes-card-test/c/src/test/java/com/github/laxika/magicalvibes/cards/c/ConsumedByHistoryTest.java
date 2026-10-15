package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsumedByHistory.class, GrizzlyBears.class})
class ConsumedByHistoryTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToEachCreatureAndPerpetuallyGrantsUnearthToNontokenDeaths() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConsumedByHistory()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player2);
        int graveyardIndex = java.util.stream.IntStream.range(0, gd.playerGraveyards.get(player2.getId()).size())
                .filter(index -> gd.playerGraveyards.get(player2.getId()).get(index).getId()
                        .equals(bears.getCard().getId()))
                .findFirst().orElseThrow();
        harness.activateGraveyardAbility(player2, graveyardIndex);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(bears.getCard().getId()));
    }

    @Test
    void grantsUnearthToSimultaneousDeathsOnBothSidesWithoutDamagingPlayers() {
        GrizzlyBears ownBears = new GrizzlyBears();
        harness.addToBattlefield(player1, ownBears);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConsumedByHistory()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateGraveyardAbility(player1, gd.playerGraveyards.get(player1.getId()).indexOf(ownBears));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.activateGraveyardAbility(player2, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void grantsUnearthToCreatureThatDiesLaterInTheSameTurn() {
        harness.setHand(player1, List.of(new ConsumedByHistory()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.activateGraveyardAbility(player2, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void perpetualUnearthRemainsUsableAfterTheTurnEnds() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConsumedByHistory()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.activateGraveyardAbility(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void doesNotGrantUnearthToCreaturesDyingOnTheNextTurn() {
        harness.setHand(player1, List.of(new ConsumedByHistory()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Card has no graveyard activated ability");
    }

}
