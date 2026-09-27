package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.Arrest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoxodonPunisher.class, LeoninScimitar.class, LightningGreaves.class, Arrest.class})
class LoxodonPunisherTest extends BaseCardTest {

    @Test
    void withoutEquipmentIs2x2() {
        Permanent punisher = addCreatureReady(player1, new LoxodonPunisher());

        assertThat(gqs.getEffectivePower(gd, punisher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, punisher)).isEqualTo(2);
    }

    @Test
    void eachAttachedEquipmentAddsTwoToPowerAndToughness() {
        Permanent punisher = addCreatureReady(player1, new LoxodonPunisher());
        Permanent scimitar1 = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent scimitar2 = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        scimitar1.setAttachedTo(punisher.getId());
        scimitar2.setAttachedTo(punisher.getId());

        assertThat(gqs.getEffectivePower(gd, punisher)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, punisher)).isEqualTo(8);
    }

    @Test
    void equipmentNotAttachedToPunisherDoesNotCount() {
        Permanent punisher = addCreatureReady(player1, new LoxodonPunisher());
        Permanent otherCreature = addCreatureReady(player1, new LoxodonPunisher());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        scimitar.setAttachedTo(otherCreature.getId());

        assertThat(gqs.getEffectivePower(gd, punisher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, punisher)).isEqualTo(2);
    }

    @Test
    void attachedEquipmentFromOpponentStillCounts() {
        Permanent punisher = addCreatureReady(player1, new LoxodonPunisher());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        scimitar.setAttachedTo(punisher.getId());

        assertThat(gqs.getEffectivePower(gd, punisher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, punisher)).isEqualTo(5);
    }

    @Test
    void equipmentWithoutPowerOrToughnessBonusStillAddsTwoEach() {
        Permanent punisher = addCreatureReady(player1, new LoxodonPunisher());
        Permanent greaves1 = harness.addToBattlefieldAndReturn(player1, new LightningGreaves());
        Permanent greaves2 = harness.addToBattlefieldAndReturn(player1, new LightningGreaves());

        greaves1.setAttachedTo(punisher.getId());
        greaves2.setAttachedTo(punisher.getId());

        assertThat(gqs.getEffectivePower(gd, punisher)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, punisher)).isEqualTo(6);
    }

    @Test
    void attachedAuraDoesNotCountAsEquipment() {
        Permanent punisher = addCreatureReady(player1, new LoxodonPunisher());
        Permanent arrest = harness.addToBattlefieldAndReturn(player1, new Arrest());
        arrest.setAttachedTo(punisher.getId());

        assertThat(gqs.getEffectivePower(gd, punisher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, punisher)).isEqualTo(2);
    }
}
