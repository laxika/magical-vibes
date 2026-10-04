package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HYDRADisintegrator.class, GrizzlyBears.class})
class HYDRADisintegratorTest extends BaseCardTest {

    @Test
    void createsMenacingVillainAndAttachesToIt() {
        castDisintegrator();

        Permanent equipment = findPermanent(player1, "HYDRA Disintegrator");
        Permanent villain = findPermanent(player1, "Villain");

        assertThat(equipment.getAttachedTo()).isEqualTo(villain.getId());
        assertThat(gqs.hasKeyword(gd, villain, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, villain)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, villain)).isEqualTo(4);
    }

    @Test
    void equipMovesTheEquipmentAndItsBoost() {
        castDisintegrator();
        Permanent equipment = findPermanent(player1, "HYDRA Disintegrator");
        Permanent villain = findPermanent(player1, "Villain");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int equipmentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(equipment);
        harness.activateAbility(player1, equipmentIndex, null, bears.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, villain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, villain)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    void secondEquipmentAttachesOnlyItselfToItsNewToken() {
        castDisintegrator();
        Permanent firstEquipment = findPermanent(player1, "HYDRA Disintegrator");
        Permanent firstVillain = findPermanent(player1, "Villain");

        castDisintegrator();

        var equipment = findPermanents(player1, "HYDRA Disintegrator");
        var villains = findPermanents(player1, "Villain");
        assertThat(equipment).hasSize(2);
        assertThat(villains).hasSize(2);
        assertThat(firstEquipment.getAttachedTo()).isEqualTo(firstVillain.getId());
        assertThat(equipment.get(1).getAttachedTo()).isEqualTo(villains.get(1).getId());
        assertThat(gqs.getEffectivePower(gd, firstVillain)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, villains.get(1))).isEqualTo(5);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        castDisintegrator();
        Permanent equipment = findPermanent(player1, "HYDRA Disintegrator");
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int equipmentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(equipment);

        assertThatThrownBy(() -> harness.activateAbility(player1, equipmentIndex, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(equipment.getAttachedTo()).isEqualTo(findPermanent(player1, "Villain").getId());
    }

    @Test
    void cannotEquipDuringCombat() {
        castDisintegrator();
        Permanent equipment = findPermanent(player1, "HYDRA Disintegrator");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        int equipmentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(equipment);

        assertThatThrownBy(() -> harness.activateAbility(player1, equipmentIndex, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(equipment.getAttachedTo()).isEqualTo(findPermanent(player1, "Villain").getId());
    }

    private void castDisintegrator() {
        harness.castFromHand(player1, new HYDRADisintegrator(), "{4}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
