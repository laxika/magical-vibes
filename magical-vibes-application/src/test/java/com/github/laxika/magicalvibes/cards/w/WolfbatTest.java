package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Wolfbat.class})
@DisplayName("Wolfbat")
class WolfbatTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard with a finality counter after its controller's second draw")
    void returnsFromGraveyardWithFinalityCounter() {
        Wolfbat wolfbat = new Wolfbat();
        harness.setGraveyard(player1, List.of(wolfbat));
        harness.setLibrary(player1, List.of(new Wolfbat(), new Wolfbat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        drawCard(player1);
        drawCard(player1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Wolfbat");
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(wolfbat.getId()));
    }

    @Test
    @DisplayName("Triggers only on the second draw each turn")
    void triggersOnlyOnSecondDrawEachTurn() {
        Wolfbat wolfbat = new Wolfbat();
        harness.setGraveyard(player1, List.of(wolfbat));
        harness.setLibrary(player1, List.of(new Wolfbat(), new Wolfbat(), new Wolfbat()));

        drawCard(player1);
        assertThat(gd.stack).isEmpty();

        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, false);

        drawCard(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A finality counter exiles it instead of putting it into a graveyard")
    void finalityCounterExilesItInsteadOfDying() {
        Permanent wolfbat = harness.addToBattlefieldAndReturn(player1, new Wolfbat());
        wolfbat.setCounterCount(CounterType.FINALITY, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, wolfbat));

        harness.assertNotOnBattlefield(player1, "Wolfbat");
        harness.assertNotInGraveyard(player1, "Wolfbat");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Wolfbat"));
    }

    @Test
    @DisplayName("Declining payment leaves Wolfbat in the graveyard and spends no mana")
    void decliningPaymentLeavesItInGraveyard() {
        harness.setGraveyard(player1, List.of(new Wolfbat()));
        harness.setLibrary(player1, List.of(new Wolfbat(), new Wolfbat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        drawCard(player1);
        drawCard(player1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Wolfbat");
        harness.assertNotOnBattlefield(player1, "Wolfbat");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot return Wolfbat by paying mana of the wrong color")
    void cannotPayWithWrongColor() {
        harness.setGraveyard(player1, List.of(new Wolfbat()));
        harness.setLibrary(player1, List.of(new Wolfbat(), new Wolfbat()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        drawCard(player1);
        drawCard(player1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Wolfbat");
        harness.assertNotOnBattlefield(player1, "Wolfbat");
    }

    @Test
    @DisplayName("An opponent's second draw does not trigger Wolfbat")
    void doesNotTriggerOnOpponentsDraw() {
        harness.setGraveyard(player1, List.of(new Wolfbat()));
        harness.setLibrary(player2, List.of(new Wolfbat(), new Wolfbat()));

        drawCard(player2);
        drawCard(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wolfbat");
    }

    @Test
    @DisplayName("An old draw trigger cannot return Wolfbat after it leaves and re-enters the graveyard")
    void oldTriggerCannotReturnNewGraveyardObject() {
        Wolfbat wolfbat = new Wolfbat();
        harness.setGraveyard(player1, List.of(wolfbat));
        harness.setLibrary(player1, List.of(new Wolfbat(), new Wolfbat()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        drawCard(player1);
        drawCard(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerGraveyards.get(player1.getId()).remove(wolfbat);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, wolfbat);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returned));

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Wolfbat");
        harness.assertInGraveyard(player1, "Wolfbat");
    }

    private void drawCard(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
