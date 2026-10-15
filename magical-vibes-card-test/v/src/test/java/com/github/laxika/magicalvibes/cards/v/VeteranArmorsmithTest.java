package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeteranArmorsmith.class, EliteVanguard.class, RuneclawBear.class})
class VeteranArmorsmithTest extends BaseCardTest {

    // ===== Static effect: buffs other Soldiers you control =====

    @Test
    @DisplayName("Other Soldier creatures you control get +0/+1")
    void buffsOtherSoldiersYouControl() {
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("Veteran Armorsmith does not buff itself")
    void doesNotBuffItself() {
        Permanent armorsmith = harness.addToBattlefieldAndReturn(player1, new VeteranArmorsmith());

        assertThat(gqs.getEffectivePower(gd, armorsmith)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, armorsmith)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-Soldier creatures")
    void doesNotBuffNonSoldiers() {
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Soldier creatures")
    void doesNotBuffOpponentSoldiers() {
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        Permanent opponentSoldier = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());

        assertThat(gqs.getEffectivePower(gd, opponentSoldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSoldier)).isEqualTo(1);
    }

    // ===== Multiple Veteran Armorsmiths =====

    @Test
    @DisplayName("Two Veteran Armorsmiths buff each other with +0/+1")
    void twoArmorsmithsBuffEachOther() {
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        harness.addToBattlefield(player1, new VeteranArmorsmith());

        List<Permanent> armorsmiths = findPermanents(player1, "Veteran Armorsmith");

        assertThat(armorsmiths).hasSize(2);
        for (Permanent armorsmith : armorsmiths) {
            assertThat(gqs.getEffectivePower(gd, armorsmith)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, armorsmith)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("Two Veteran Armorsmiths give +0/+2 to other Soldiers")
    void twoArmorsmithsStackBonuses() {
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());

        // 2/1 base + 0/2 from two armorsmiths = 2/3
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(3);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Veteran Armorsmith leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());

        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Veteran Armorsmith"));

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus applies when Veteran Armorsmith resolves onto battlefield")
    void bonusAppliesOnResolve() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);

        harness.setHand(player1, List.of(new VeteranArmorsmith()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("Soldiers entering after Veteran Armorsmith receive the bonus")
    void buffsSoldierEnteringLater() {
        harness.addToBattlefield(player1, new VeteranArmorsmith());
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Elite Vanguard");
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
    }
}
