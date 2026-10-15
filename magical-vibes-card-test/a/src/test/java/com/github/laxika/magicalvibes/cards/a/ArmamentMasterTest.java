package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.n.NimbusWings;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmamentMaster.class, GrizzlyBears.class, LeoninScimitar.class, NimbusWings.class})
class ArmamentMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Other Kor creatures get +2/+2 for each Equipment attached to Armament Master")
    void boostsOtherKorCreaturesForEachAttachedEquipment() {
        Permanent master = addMasterReady(player1);
        Permanent otherKor = addMasterReady(player1);
        Permanent scimitar1 = addScimitarReady(player1);
        Permanent scimitar2 = addScimitarReady(player1);
        scimitar1.setAttachedTo(master.getId());
        scimitar2.setAttachedTo(master.getId());

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherKor)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, otherKor)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not boost non-Kor creatures")
    void doesNotBoostNonKorCreatures() {
        Permanent master = addMasterReady(player1);
        Permanent bears = addBearsReady(player1);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(master.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only Equipment attached to Armament Master counts")
    void onlyEquipmentAttachedToMasterCounts() {
        Permanent master = addMasterReady(player1);
        Permanent bears = addBearsReady(player1);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(2);
    }

    @Test
    void doesNotBoostOpponentsKorCreatures() {
        Permanent master = addMasterReady(player1);
        Permanent opposingKor = addMasterReady(player2);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(master.getId());

        assertThat(gqs.getEffectivePower(gd, opposingKor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingKor)).isEqualTo(2);
    }

    @Test
    void countsEquipmentControlledByOpponent() {
        Permanent master = addMasterReady(player1);
        Permanent otherKor = addMasterReady(player1);
        Permanent scimitar = addScimitarReady(player2);
        scimitar.setAttachedTo(master.getId());

        assertThat(gqs.getEffectivePower(gd, otherKor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherKor)).isEqualTo(4);
    }

    @Test
    void updatesBoostWhenEquipmentMovesAndSourceLeaves() {
        Permanent master = addMasterReady(player1);
        Permanent otherKor = addMasterReady(player1);
        Permanent scimitar = addScimitarReady(player1);

        assertThat(gqs.getEffectivePower(gd, otherKor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherKor)).isEqualTo(2);

        scimitar.setAttachedTo(master.getId());
        assertThat(gqs.getEffectivePower(gd, otherKor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherKor)).isEqualTo(4);

        scimitar.setAttachedTo(otherKor.getId());
        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherKor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherKor)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(otherKor);
        scimitar.setAttachedTo(null);
        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(2);
    }

    @Test
    void attachedAurasDoNotIncreaseKorBoost() {
        Permanent master = addMasterReady(player1);
        Permanent otherKor = addMasterReady(player1);
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new NimbusWings());
        wings.setAttachedTo(master.getId());
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(master.getId());

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherKor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherKor)).isEqualTo(4);
    }

    private Permanent addMasterReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ArmamentMaster());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addBearsReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addScimitarReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
        perm.setSummoningSick(false);
        return perm;
    }
}
