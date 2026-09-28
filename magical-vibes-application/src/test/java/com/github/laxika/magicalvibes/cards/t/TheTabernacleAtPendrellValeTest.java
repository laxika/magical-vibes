package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelSentinel;
import com.github.laxika.magicalvibes.cards.s.SeafarersQuay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheTabernacleAtPendrellVale.class, TundraWolves.class, SeafarersQuay.class, DarksteelSentinel.class})
class TheTabernacleAtPendrellValeTest extends BaseCardTest {

    private void addTabernacle(Player controller) {
        harness.addToBattlefield(controller, new TheTabernacleAtPendrellVale());
    }

    private Permanent addWolves(Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new TundraWolves());
    }

    @Test
    @DisplayName("Declining to pay {1} destroys the creature")
    void decliningPaymentDestroysCreature() {
        addTabernacle(player1);
        addWolves(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Tundra Wolves");
        harness.assertInGraveyard(player1, "Tundra Wolves");
    }

    @Test
    @DisplayName("Paying {1} keeps the creature on the battlefield")
    void payingKeepsCreature() {
        addTabernacle(player1);
        addWolves(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Tundra Wolves");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Grant is global: an opponent's Tabernacle still taxes your creature")
    void opponentsTabernacleTaxesYourCreature() {
        addTabernacle(player2);
        addWolves(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Tundra Wolves");
    }

    @Test
    @DisplayName("An opponent's creature does not trigger during your upkeep")
    void opponentCreatureNotTriggeredDuringYourUpkeep() {
        addTabernacle(player1);
        addWolves(player2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Tundra Wolves");
    }

    @Test
    @DisplayName("Non-creature permanents are unaffected")
    void nonCreatureUnaffected() {
        addTabernacle(player1);
        harness.addToBattlefield(player1, new SeafarersQuay());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Seafarer's Quay");
    }

    @Test
    @DisplayName("Declining to pay cannot destroy an indestructible creature")
    void decliningPaymentCannotDestroyIndestructibleCreature() {
        addTabernacle(player1);
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new DarksteelSentinel());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sentinel);
    }

    @Test
    @DisplayName("A creature is taxed during its own controller's upkeep")
    void creatureIsTaxedDuringItsControllersUpkeep() {
        addTabernacle(player1);
        addWolves(player2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Tundra Wolves");
        harness.assertInGraveyard(player2, "Tundra Wolves");
    }

    @Test
    @DisplayName("Each controlled creature gets its own upkeep trigger")
    void eachControlledCreatureGetsItsOwnUpkeepTrigger() {
        addTabernacle(player1);
        addWolves(player1);
        addWolves(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Tundra Wolves"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Tundra Wolves"))
                .hasSize(2);
    }
}
