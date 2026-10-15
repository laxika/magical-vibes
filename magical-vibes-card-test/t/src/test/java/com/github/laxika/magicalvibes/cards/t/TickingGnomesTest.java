package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DevoutHarpist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TickingGnomes.class, DevoutHarpist.class})
class TickingGnomesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Ticking Gnomes deals 1 damage to a player")
    void dealsDamageToPlayer() {
        harness.addToBattlefield(player1, new TickingGnomes());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Ticking Gnomes");
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Ticking Gnomes");
    }

    @Test
    @DisplayName("The activated ability can target a creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new TickingGnomes());
        harness.addToBattlefield(player2, new DevoutHarpist());

        harness.activateAbility(player1, 0, null, findPermanent(player2, "Devout Harpist").getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Devout Harpist");
        harness.assertInGraveyard(player2, "Devout Harpist");
    }

    @Test
    @DisplayName("Declining echo sacrifices Ticking Gnomes at its next upkeep")
    void decliningEchoSacrificesGnomes() {
        castAndResolveGnomes();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Ticking Gnomes");
        harness.assertInGraveyard(player1, "Ticking Gnomes");
    }

    @Test
    @DisplayName("Paying echo keeps Ticking Gnomes and echo does not trigger again")
    void payingEchoKeepsGnomesAndIsOneShot() {
        castAndResolveGnomes();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Ticking Gnomes");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Ticking Gnomes");
    }

    @Test
    @DisplayName("Echo does not trigger during the opponent's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndResolveGnomes();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Ticking Gnomes");
    }

    @Test
    @DisplayName("Entering the battlefield does not put an echo registration ability on the stack")
    void enteringDoesNotCreateEchoRegistrationTrigger() {
        harness.castFromHand(player1, new TickingGnomes(), "{3}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ticking Gnomes");
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Ticking Gnomes");
    }

    @Test
    @DisplayName("Ticking Gnomes can sacrifice itself immediately to damage its controller")
    void canDamageItsControllerWhileSummoningSick() {
        harness.addToBattlefield(player1, new TickingGnomes());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.assertInGraveyard(player1, "Ticking Gnomes");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Sacrificing Gnomes before upkeep leaves no echo ability to resolve")
    void sacrificedGnomesDoNotTriggerEcho() {
        castAndResolveGnomes();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Ticking Gnomes");
    }
    private void castAndResolveGnomes() {
        harness.castFromHand(player1, new TickingGnomes(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
