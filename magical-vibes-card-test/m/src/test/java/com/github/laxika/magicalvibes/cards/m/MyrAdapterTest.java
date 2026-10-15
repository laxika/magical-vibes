package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.a.Arrest;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyrAdapter.class, LeoninScimitar.class, AlphaMyr.class, Arrest.class})
class MyrAdapterTest extends BaseCardTest {

    @Test
    void withoutAttachedEquipmentIs1x1() {
        Permanent adapter = addAdapterReady(player1);

        assertThat(gqs.getEffectivePower(gd, adapter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adapter)).isEqualTo(1);
    }

    @Test
    void eachAttachedEquipmentAddsOneToPowerAndToughness() {
        Permanent adapter = addAdapterReady(player1);
        Permanent scimitar1 = addScimitarReady(player1);
        Permanent scimitar2 = addScimitarReady(player1);

        scimitar1.setAttachedTo(adapter.getId());
        scimitar2.setAttachedTo(adapter.getId());

        assertThat(gqs.getEffectivePower(gd, adapter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, adapter)).isEqualTo(5);
    }

    @Test
    void equipmentNotAttachedToAdapterDoesNotCount() {
        Permanent adapter = addAdapterReady(player1);
        Permanent otherCreature = addCreatureReady(player1, new AlphaMyr());
        Permanent scimitar = addScimitarReady(player1);

        scimitar.setAttachedTo(otherCreature.getId());

        assertThat(gqs.getEffectivePower(gd, adapter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adapter)).isEqualTo(1);
    }

    @Test
    void attachedEquipmentFromOpponentStillCounts() {
        Permanent adapter = addAdapterReady(player1);
        Permanent scimitar = addScimitarReady(player2);
        scimitar.setAttachedTo(adapter.getId());

        assertThat(gqs.getEffectivePower(gd, adapter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, adapter)).isEqualTo(3);
    }

    @Test
    void attachedAuraDoesNotCountAsEquipment() {
        Permanent adapter = addAdapterReady(player1);
        Permanent arrest = harness.addToBattlefieldAndReturn(player1, new Arrest());

        arrest.setAttachedTo(adapter.getId());

        assertThat(gqs.getEffectivePower(gd, adapter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adapter)).isEqualTo(1);
    }

    @Test
    void unattachedEquipmentDoesNotCount() {
        Permanent adapter = addAdapterReady(player1);
        addScimitarReady(player1);

        assertThat(gqs.getEffectivePower(gd, adapter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adapter)).isEqualTo(1);
    }

    private Permanent addAdapterReady(Player player) {
        return addCreatureReady(player, new MyrAdapter());
    }

    private Permanent addScimitarReady(Player player) {
        return addCreatureReady(player, new LeoninScimitar());
    }

    @Test
    void movingEquipmentAwayRemovesItsBonusAndTheAdapterBonus() {
        Permanent adapter = addAdapterReady(player1);
        Permanent otherCreature = addCreatureReady(player1, new AlphaMyr());
        Permanent scimitar = addScimitarReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, adapter.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, adapter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, adapter)).isEqualTo(3);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, null, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(otherCreature.getId());
        assertThat(gqs.getEffectivePower(gd, adapter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adapter)).isEqualTo(1);
    }

    @Test
    void equipmentLeavingBattlefieldNoLongerCounts() {
        Permanent adapter = addAdapterReady(player1);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(adapter.getId());

        assertThat(gqs.getEffectivePower(gd, adapter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, adapter)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(scimitar);
        gd.playerGraveyards.get(player1.getId()).add(scimitar.getCard());

        assertThat(gqs.getEffectivePower(gd, adapter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adapter)).isEqualTo(1);
    }
}
