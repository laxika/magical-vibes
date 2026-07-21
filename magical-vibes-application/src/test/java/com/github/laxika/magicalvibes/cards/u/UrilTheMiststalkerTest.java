package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Uril, the Miststalker")
class UrilTheMiststalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Without any Aura attached, is 5/5")
    void withoutAurasIs5x5() {
        Permanent uril = addUrilReady(player1);

        assertThat(gqs.getEffectivePower(gd, uril)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, uril)).isEqualTo(5);
    }

    @Test
    @DisplayName("With one Aura attached, gets +2/+2 plus the Aura's stats")
    void withOneAura() {
        Permanent uril = addUrilReady(player1);
        Permanent aura = addAuraReady(player1);
        aura.setAttachedTo(uril.getId());

        // Base 5/5 + 2/2 from Uril (one Aura) + 1/2 from Holy Strength = 8/9
        assertThat(gqs.getEffectivePower(gd, uril)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, uril)).isEqualTo(9);
    }

    @Test
    @DisplayName("With two Auras attached, gets +4/+4")
    void withTwoAuras() {
        Permanent uril = addUrilReady(player1);
        Permanent aura1 = addAuraReady(player1);
        Permanent aura2 = addAuraReady(player1);
        aura1.setAttachedTo(uril.getId());
        aura2.setAttachedTo(uril.getId());

        // Base 5/5 + 4/4 from Uril (two Auras) + 2/4 from two Holy Strength = 11/13
        assertThat(gqs.getEffectivePower(gd, uril)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, uril)).isEqualTo(13);
    }

    @Test
    @DisplayName("Attached Equipment does not count toward Uril's bonus")
    void equipmentDoesNotCount() {
        Permanent uril = addUrilReady(player1);
        Permanent scimitar = addEquipmentReady(player1);
        scimitar.setAttachedTo(uril.getId());

        // Base 5/5 + 0 from Uril (Equipment is not an Aura) + 1/1 from Leonin Scimitar = 6/6
        assertThat(gqs.getEffectivePower(gd, uril)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, uril)).isEqualTo(6);
    }

    @Test
    @DisplayName("Auras on other creatures do not count")
    void aurasOnOtherCreaturesDoNotCount() {
        Permanent uril = addUrilReady(player1);
        Permanent other = addUrilReady(player1);
        Permanent aura = addAuraReady(player1);
        aura.setAttachedTo(other.getId());

        assertThat(gqs.getEffectivePower(gd, uril)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, uril)).isEqualTo(5);
    }

    // ===== Helpers =====

    private Permanent addUrilReady(Player player) {
        Permanent perm = new Permanent(new UrilTheMiststalker());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAuraReady(Player player) {
        Permanent perm = new Permanent(new HolyStrength());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addEquipmentReady(Player player) {
        Permanent perm = new Permanent(new LeoninScimitar());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
