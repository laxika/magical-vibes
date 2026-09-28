package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void castDisintegrator() {
        harness.setHand(player1, List.of(new HYDRADisintegrator()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
