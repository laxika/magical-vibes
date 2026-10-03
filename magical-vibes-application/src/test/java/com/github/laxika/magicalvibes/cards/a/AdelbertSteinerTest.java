package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdelbertSteiner.class, LeoninScimitar.class})
class AdelbertSteinerTest extends BaseCardTest {

    @Test
    @DisplayName("Adelbert Steiner gets +1/+1 for each Equipment you control")
    void getsBoostForEachControlledEquipment() {
        Permanent steiner = harness.addToBattlefieldAndReturn(player1, new AdelbertSteiner());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, steiner)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, steiner)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent-controlled Equipment does not boost Adelbert Steiner")
    void opponentEquipmentDoesNotCount() {
        Permanent steiner = harness.addToBattlefieldAndReturn(player1, new AdelbertSteiner());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, steiner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steiner)).isEqualTo(1);
    }

    @Test
    void boostUpdatesWhenEquipmentLeavesTheBattlefield() {
        Permanent steiner = harness.addToBattlefieldAndReturn(player1, new AdelbertSteiner());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, steiner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, steiner)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        harness.setGraveyard(player1, java.util.List.of(equipment.getCard()));

        assertThat(gqs.getEffectivePower(gd, steiner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steiner)).isEqualTo(1);
    }

    @Test
    void boostUsesCurrentEquipmentControllerRatherThanOwnerOrAttachment() {
        Permanent steiner = harness.addToBattlefieldAndReturn(player1, new AdelbertSteiner());
        Permanent opposingSteiner = harness.addToBattlefieldAndReturn(player2, new AdelbertSteiner());
        LeoninScimitar card = new LeoninScimitar();
        card.setOwnerId(player1.getId());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, card);
        equipment.setAttachedTo(opposingSteiner.getId());

        assertThat(gqs.getEffectivePower(gd, steiner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, steiner)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingSteiner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingSteiner)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        gd.playerBattlefields.get(player2.getId()).add(equipment);

        assertThat(gqs.getEffectivePower(gd, steiner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steiner)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingSteiner)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingSteiner)).isEqualTo(3);
    }

    @Test
    void lifelinkGainsLifeEqualToEquipmentBoostedCombatDamage() {
        Permanent steiner = addCreatureReady(player1, new AdelbertSteiner());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        steiner.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }
}
