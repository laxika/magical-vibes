package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PendrellMists.class, BenalishInfantry.class, MindStone.class})
class PendrellMistsTest extends BaseCardTest {

    private void addMists(Player controller) {
        harness.addToBattlefield(controller, new PendrellMists());
    }

    private Permanent addInfantry(Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new BenalishInfantry());
    }

    @Test
    @DisplayName("Declining to pay {1} sacrifices the creature")
    void decliningPaymentSacrificesCreature() {
        addMists(player1);
        addInfantry(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Benalish Infantry");
        harness.assertInGraveyard(player1, "Benalish Infantry");
    }

    @Test
    @DisplayName("Paying {1} keeps the creature on the battlefield")
    void payingKeepsCreature() {
        addMists(player1);
        addInfantry(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Benalish Infantry");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Accepting without enough mana still sacrifices the creature")
    void acceptingWithoutEnoughManaSacrificesCreature() {
        addMists(player1);
        addInfantry(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Benalish Infantry");
        harness.assertInGraveyard(player1, "Benalish Infantry");
    }

    @Test
    @DisplayName("Grant is global: an opponent's Pendrell Mists still taxes your creature")
    void opponentsMistsTaxesYourCreature() {
        addMists(player2);
        addInfantry(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Benalish Infantry");
    }

    @Test
    @DisplayName("An opponent's creature does not trigger during your upkeep")
    void opponentCreatureNotTriggeredDuringYourUpkeep() {
        addMists(player1);
        Permanent opponentInfantry = addInfantry(player2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(opponentInfantry.getId()));
    }

    @Test
    @DisplayName("Non-creature permanents are unaffected")
    void nonCreatureUnaffected() {
        addMists(player1);
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(mindStone.getId()));
    }
}
