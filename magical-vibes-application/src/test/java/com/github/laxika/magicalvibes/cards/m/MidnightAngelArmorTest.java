package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MidnightAngelArmor.class, GrizzlyBears.class, Spellbook.class})
class MidnightAngelArmorTest extends BaseCardTest {

    @Test
    void entersWithSoldierTokenAttachedAndEmpowersIt() {
        castArmor();

        Permanent armor = findPermanent(player1, "Midnight Angel Armor");
        Permanent soldier = findPermanent(player1, "Soldier");

        assertThat(armor.getAttachedTo()).isEqualTo(soldier.getId());
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void canEquipAnotherCreature() {
        castArmor();
        Permanent armor = findPermanent(player1, "Midnight Angel Armor");
        Permanent soldier = findPermanent(player1, "Soldier");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int armorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(armor);
        harness.activateAbility(player1, armorIndex, null, bears.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void equipCannotTargetNonCreaturePermanent() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new MidnightAngelArmor());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int armorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(armor);
        assertThatThrownBy(() -> harness.activateAbility(player1, armorIndex, null, spellbook.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castArmor() {
        harness.setHand(player1, List.of(new MidnightAngelArmor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
