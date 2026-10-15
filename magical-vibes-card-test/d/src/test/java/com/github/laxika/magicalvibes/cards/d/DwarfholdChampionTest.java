package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.ThievesTools;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwarfholdChampion.class, ThievesTools.class})
class DwarfholdChampionTest extends BaseCardTest {

    @Test
    void withoutEquipmentHasPrintedToughness() {
        Permanent champion = addChampionReady(player1);

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(1);
    }

    @Test
    void whileEquippedGetsToughnessBoost() {
        Permanent champion = addChampionReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(champion.getId());

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);
    }

    @Test
    void losesToughnessBoostWhenEquipmentIsDetached() {
        Permanent champion = addChampionReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(champion.getId());

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(1);
    }

    private Permanent addChampionReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new DwarfholdChampion());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addEquipmentReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ThievesTools());
    }

    @Test
    void multipleEquipmentGrantOnlyOneBoostAndLastDetachmentRemovesIt() {
        Permanent champion = addChampionReady(player1);
        Permanent first = addEquipmentReady(player1);
        Permanent second = addEquipmentReady(player1);
        first.setAttachedTo(champion.getId());
        second.setAttachedTo(champion.getId());

        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);

        first.setAttachedTo(null);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);

        second.setAttachedTo(null);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(1);
    }

    @Test
    void equipmentControlledByOpponentStillEnablesBoost() {
        Permanent champion = addChampionReady(player1);
        Permanent equipment = addEquipmentReady(player2);
        equipment.setAttachedTo(champion.getId());

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);
    }

    @Test
    void movingEquipmentTransfersBoostOnlyToEquippedChampion() {
        Permanent first = addChampionReady(player1);
        Permanent second = addChampionReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(first.getId());

        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);

        equipment.setAttachedTo(second.getId());

        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }
}
