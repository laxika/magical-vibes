package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AngelicArmamentsTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {4} attaches the Equipment to a creature you control")
    void equipAttachesToCreature() {
        harness.addToBattlefield(player1, new AngelicArmaments());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        Permanent armor = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(armor.getAttachedTo()).isEqualTo(bearsId);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature gets +2/+2 and has flying")
    void equippedCreatureGetsBoostAndFlying() {
        Permanent armor = new Permanent(new AngelicArmaments());
        gd.playerBattlefields.get(player1.getId()).add(armor);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();

        armor.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature becomes white in addition to its other colors")
    void equippedCreatureBecomesWhiteAdditively() {
        Permanent armor = new Permanent(new AngelicArmaments());
        gd.playerBattlefields.get(player1.getId()).add(armor);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThat(gqs.hasColor(gd, bears, CardColor.WHITE)).isFalse();

        armor.setAttachedTo(bears.getId());

        assertThat(gqs.hasColor(gd, bears, CardColor.WHITE)).isTrue();
        // Green (its printed colour) is retained — the grant is additive
        assertThat(gqs.getEffectiveColors(gd, bears)).contains(CardColor.GREEN, CardColor.WHITE);
    }

    @Test
    @DisplayName("Equipped creature becomes an Angel in addition to its other types")
    void equippedCreatureBecomesAngel() {
        Permanent armor = new Permanent(new AngelicArmaments());
        gd.playerBattlefields.get(player1.getId()).add(armor);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        armor.setAttachedTo(bears.getId());

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bears);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.ANGEL);
        assertThat(bonus.subtypeOverriding()).isFalse();
        // Printed Bear subtype is untouched
        assertThat(bears.getCard().getSubtypes()).contains(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("All grants wear off when the Equipment is unattached")
    void grantsRemovedWhenUnequipped() {
        Permanent armor = new Permanent(new AngelicArmaments());
        gd.playerBattlefields.get(player1.getId()).add(armor);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        armor.setAttachedTo(bears.getId());
        armor.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasColor(gd, bears, CardColor.WHITE)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, bears).grantedSubtypes()).doesNotContain(CardSubtype.ANGEL);
    }
}
