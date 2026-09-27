package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WizardReplica.class, YotianSoldier.class, Shatter.class})
class WizardReplicaTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell unless its controller pays {2}")
    void countersCreatureSpell() {
        WizardReplica replica = new WizardReplica();
        harness.addToBattlefield(player1, replica);

        YotianSoldier soldier = new YotianSoldier();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, soldier, "{3}");
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, soldier.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Yotian Soldier");
        harness.assertInGraveyard(player1, "Wizard Replica");
        harness.assertNotOnBattlefield(player2, "Yotian Soldier");
    }

    @Test
    @DisplayName("Allows the spell to resolve when its controller pays {2}")
    void allowsPayment() {
        WizardReplica replica = new WizardReplica();
        harness.addToBattlefield(player1, replica);

        var target = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        Shatter shatter = new Shatter();
        harness.setHand(player2, List.of(shatter));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, target.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shatter.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Yotian Soldier");
        harness.assertNotOnBattlefield(player1, "Yotian Soldier");
        harness.assertInGraveyard(player1, "Wizard Replica");
    }

    @Test
    @DisplayName("Counters the spell when its controller declines to pay {2}")
    void countersWhenPaymentIsDeclined() {
        WizardReplica replica = new WizardReplica();
        harness.addToBattlefield(player1, replica);

        YotianSoldier soldier = new YotianSoldier();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, soldier, "{3}");
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, soldier.getId());
        harness.assertInGraveyard(player1, "Wizard Replica");
        harness.assertNotOnBattlefield(player1, "Wizard Replica");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Yotian Soldier");
        harness.assertNotOnBattlefield(player2, "Yotian Soldier");
    }

    @Test
    @DisplayName("Cannot target a permanent with the activated ability")
    void cannotTargetPermanent() {
        WizardReplica replica = new WizardReplica();
        harness.addToBattlefield(player1, replica);
        YotianSoldier soldier = new YotianSoldier();
        harness.addToBattlefield(player2, soldier);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, soldier.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without blue mana")
    void cannotActivateWithoutBlueMana() {
        WizardReplica replica = new WizardReplica();
        harness.addToBattlefield(player1, replica);

        YotianSoldier soldier = new YotianSoldier();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, soldier, "{3}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, soldier.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Wizard Replica");
        assertThat(gd.stack).hasSize(1);
    }
}
