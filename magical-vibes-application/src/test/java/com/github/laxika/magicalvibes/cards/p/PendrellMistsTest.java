package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PendrellMistsTest extends BaseCardTest {

    private void addMists(Player controller) {
        gd.playerBattlefields.get(controller.getId()).add(new Permanent(new PendrellMists()));
    }

    private Permanent addBears(Player controller) {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(controller.getId()).add(bears);
        return bears;
    }

    @Test
    @DisplayName("Declining to pay {1} sacrifices the creature")
    void decliningPaymentSacrificesCreature() {
        addMists(player1);
        addBears(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Paying {1} keeps the creature on the battlefield")
    void payingKeepsCreature() {
        addMists(player1);
        addBears(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Grant is global: an opponent's Pendrell Mists still taxes your creature")
    void opponentsMistsTaxesYourCreature() {
        addMists(player2);
        addBears(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's creature does not trigger during your upkeep")
    void opponentCreatureNotTriggeredDuringYourUpkeep() {
        addMists(player1);
        Permanent opponentBears = addBears(player2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(opponentBears.getId()));
    }

    @Test
    @DisplayName("Non-creature permanents are unaffected")
    void nonCreatureUnaffected() {
        addMists(player1);
        Permanent fountain = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player1.getId()).add(fountain);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(fountain.getId()));
    }
}
