package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.m.Murder;
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

@CardUsed({FirebladeCharger.class, Bonesplitter.class, Murder.class})
class FirebladeChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Fireblade Charger has haste while equipped")
    void equippedFirebladeChargerHasHaste() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new FirebladeCharger());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
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
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(charger.getId());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, charger.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Fireblade Charger");
    }
}
