package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BloodchiefsThirst;
import com.github.laxika.magicalvibes.cards.e.ExpeditionChampion;
import com.github.laxika.magicalvibes.cards.j.JaceMirrorMage;
import com.github.laxika.magicalvibes.cards.s.ScavengedBlade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirebladeCharger.class, ScavengedBlade.class, BloodchiefsThirst.class,
        ExpeditionChampion.class, JaceMirrorMage.class})
class FirebladeChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Fireblade Charger has haste while equipped")
    void equippedFirebladeChargerHasHaste() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new FirebladeCharger());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new ScavengedBlade());
        equipment.setAttachedTo(charger.getId());

        assertThat(gqs.hasKeyword(gd, charger, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Fireblade Charger does not have haste while unequipped")
    void unequippedFirebladeChargerDoesNotHaveHaste() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new FirebladeCharger());

        assertThat(gqs.hasKeyword(gd, charger, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Fireblade Charger deals damage equal to its last known power when it dies")
    void deathTriggerDealsLastKnownPowerDamage() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new FirebladeCharger());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new ScavengedBlade());
        equipment.setAttachedTo(charger.getId());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodchiefsThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, charger.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Fireblade Charger");
    }

    @Test
    void losesHasteWhenEquipmentIsDetached() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new FirebladeCharger());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new ScavengedBlade());
        equipment.setAttachedTo(charger.getId());

        assertThat(gqs.hasKeyword(gd, charger, Keyword.HASTE)).isTrue();
        equipment.setAttachedTo(null);
        assertThat(gqs.hasKeyword(gd, charger, Keyword.HASTE)).isFalse();
    }

    @Test
    void unequippedDeathTriggerCanDamageItsController() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new FirebladeCharger());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BloodchiefsThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, charger.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Fireblade Charger");
    }

    @Test
    void deathTriggerCanDamageACreature() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new FirebladeCharger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ExpeditionChampion());
        harness.setHand(player1, List.of(new BloodchiefsThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, charger.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Expedition Champion");
    }

    @Test
    void deathTriggerCanDamageAPlaneswalker() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new FirebladeCharger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JaceMirrorMage());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new BloodchiefsThirst()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, charger.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }
}
