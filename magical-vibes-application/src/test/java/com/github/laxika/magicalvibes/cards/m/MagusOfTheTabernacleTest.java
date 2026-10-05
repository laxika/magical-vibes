package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheTabernacle.class, GossamerPhantasm.class, UrborgTombOfYawgmoth.class})
class MagusOfTheTabernacleTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay {1} sacrifices the Magus itself")
    void decliningPaymentSacrificesMagus() {
        harness.addToBattlefieldAndReturn(player1, new MagusOfTheTabernacle());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Magus of the Tabernacle");
        harness.assertInGraveyard(player1, "Magus of the Tabernacle");
    }

    @Test
    @DisplayName("Paying {1} keeps the Magus on the battlefield")
    void payingKeepsMagus() {
        harness.addToBattlefieldAndReturn(player1, new MagusOfTheTabernacle());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Magus of the Tabernacle");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Each creature gets an independent pay-or-sacrifice choice")
    void eachCreatureGetsIndependentChoice() {
        harness.addToBattlefieldAndReturn(player1, new MagusOfTheTabernacle());
        harness.addToBattlefieldAndReturn(player1, new GossamerPhantasm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertOnBattlefield(player1, "Magus of the Tabernacle");
    }

    @Test
    @DisplayName("The ability triggers during each creature controller's upkeep")
    void triggersDuringEachCreatureControllersUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new MagusOfTheTabernacle());
        harness.addToBattlefieldAndReturn(player2, new GossamerPhantasm());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Gossamer Phantasm");
        harness.assertOnBattlefield(player1, "Magus of the Tabernacle");
    }

    @Test
    @DisplayName("The ability does not affect noncreature permanents")
    void doesNotAffectNoncreaturePermanents() {
        harness.addToBattlefieldAndReturn(player1, new MagusOfTheTabernacle());
        harness.addToBattlefieldAndReturn(player1, new UrborgTombOfYawgmoth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Urborg, Tomb of Yawgmoth");
    }

    @Test
    @DisplayName("Granted upkeep triggers still resolve after the Magus is sacrificed")
    void pendingTriggerSurvivesMagusLeaving() {
        harness.addToBattlefield(player1, new GossamerPhantasm());
        harness.addToBattlefield(player1, new MagusOfTheTabernacle());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Magus of the Tabernacle");
        harness.assertOnBattlefield(player1, "Gossamer Phantasm");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Gossamer Phantasm");
    }

    @Test
    @DisplayName("Two Magi require two independent payments for each creature")
    void multipleMagiGrantMultipleUpkeepAbilities() {
        harness.addToBattlefield(player1, new MagusOfTheTabernacle());
        harness.addToBattlefield(player2, new MagusOfTheTabernacle());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Magus of the Tabernacle");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Magus of the Tabernacle");
        harness.assertOnBattlefield(player2, "Magus of the Tabernacle");
    }
}
