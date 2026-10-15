package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.a.Arrest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinGaveleer.class, LeoninScimitar.class, AccordersShield.class, Arrest.class})
class GoblinGaveleerTest extends BaseCardTest {

    @Test
    @DisplayName("Without equipment, is 1/1")
    void withoutEquipmentIs1x1() {
        harness.setHand(player1, List.of(new GoblinGaveleer()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent gaveleer = findPermanent(player1, "Goblin Gaveleer");
        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gaveleer)).isEqualTo(1);
    }

    @Test
    @DisplayName("With one equipment attached, is 4/2 (+2/+0 from Gaveleer, +1/+1 from Scimitar)")
    void withOneEquipmentIs4x2() {
        Permanent gaveleer = addGaveleerReady(player1);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(gaveleer.getId());

        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gaveleer)).isEqualTo(2);
    }

    @Test
    @DisplayName("With two equipment attached, is 7/3 (+4/+0 from Gaveleer, +2/+2 from two Scimitars)")
    void withTwoEquipmentIs7x3() {
        Permanent gaveleer = addGaveleerReady(player1);
        Permanent scimitar1 = addScimitarReady(player1);
        Permanent scimitar2 = addScimitarReady(player1);

        scimitar1.setAttachedTo(gaveleer.getId());
        scimitar2.setAttachedTo(gaveleer.getId());

        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, gaveleer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipment on other creatures doesn't count for Goblin Gaveleer's bonus")
    void equipmentOnOtherCreaturesDoesntCount() {
        Permanent gaveleer = addGaveleerReady(player1);
        Permanent otherCreature = addGaveleerReady(player1);
        Permanent scimitar = addScimitarReady(player1);

        scimitar.setAttachedTo(otherCreature.getId());

        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gaveleer)).isEqualTo(1);
    }

    @Test
    @DisplayName("Unattached equipment on battlefield doesn't affect Goblin Gaveleer")
    void unattachedEquipmentDoesntCount() {
        Permanent gaveleer = addGaveleerReady(player1);
        addScimitarReady(player1);

        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gaveleer)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's equipment DOES affect Goblin Gaveleer (+2/+0 from Gaveleer, +1/+1 from Scimitar)")
    void opponentEquipmentAffects() {
        Permanent gaveleer = addGaveleerReady(player1);
        Permanent scimitar = addScimitarReady(player2);
        scimitar.setAttachedTo(gaveleer.getId());

        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gaveleer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving re-equip transfers Gaveleer's bonus immediately")
    void resolvingReEquipTransfersBonus() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent first = addGaveleerReady(player1);
        Permanent second = addGaveleerReady(player1);
        shield.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipment leaving the battlefield removes its contribution immediately")
    void removedEquipmentStopsCounting() {
        Permanent gaveleer = addGaveleerReady(player1);
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        shield.setAttachedTo(gaveleer.getId());
        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(shield);

        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gaveleer)).isEqualTo(1);
    }

    @Test
    @DisplayName("An attached Aura does not contribute to the Equipment bonus")
    void attachedAuraDoesNotCount() {
        Permanent gaveleer = addGaveleerReady(player1);
        Permanent arrest = harness.addToBattlefieldAndReturn(player2, new Arrest());
        arrest.setAttachedTo(gaveleer.getId());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        shield.setAttachedTo(gaveleer.getId());

        assertThat(gqs.getEffectivePower(gd, gaveleer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gaveleer)).isEqualTo(4);
    }

    private Permanent addGaveleerReady(Player player) {
        return addCreatureReady(player, new GoblinGaveleer());
    }

    private Permanent addScimitarReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
        perm.setSummoningSick(false);
        return perm;
    }

}
