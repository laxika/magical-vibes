package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WildbornPreserver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrystalSlipper.class, WildbornPreserver.class})
class CrystalSlipperTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 and haste")
    void equippedCreatureGetsBoostAndHaste() {
        Permanent slipper = addReadySlipper(player1);
        Permanent bears = addCreatureReady(player1, new WildbornPreserver());
        slipper.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Unattached Crystal Slipper does not affect creatures")
    void unattachedSlipperDoesNotAffectCreatures() {
        Permanent bears = addCreatureReady(player1, new WildbornPreserver());
        addReadySlipper(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Equip ability attaches Crystal Slipper to a creature")
    void equipAttachesSlipper() {
        Permanent slipper = addReadySlipper(player1);
        Permanent bears = addCreatureReady(player1, new WildbornPreserver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(slipper.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Re-equipping transfers the boost and haste only on resolution")
    void reEquipTransfersBenefitsOnResolution() {
        Permanent slipper = addReadySlipper(player1);
        Permanent first = addCreatureReady(player1, new WildbornPreserver());
        Permanent second = addCreatureReady(player1, new WildbornPreserver());
        slipper.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(slipper.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        assertThat(slipper.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A failed re-equip preserves the original creature's benefits")
    void failedReEquipPreservesBenefits() {
        Permanent slipper = addReadySlipper(player1);
        Permanent first = addCreatureReady(player1, new WildbornPreserver());
        Permanent second = addCreatureReady(player1, new WildbornPreserver());
        slipper.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(slipper.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing Crystal Slipper removes its boost and haste")
    void removingSlipperRemovesBenefits() {
        Permanent slipper = addReadySlipper(player1);
        Permanent bears = addCreatureReady(player1, new WildbornPreserver());
        slipper.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(slipper);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        addReadySlipper(player1);
        Permanent bears = addCreatureReady(player2, new WildbornPreserver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip cannot be activated during an opponent's turn")
    void cannotEquipDuringOpponentsTurn() {
        addReadySlipper(player1);
        Permanent bears = addCreatureReady(player1, new WildbornPreserver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadySlipper(Player player) {
        return addCreatureReady(player, new CrystalSlipper());
    }
}
