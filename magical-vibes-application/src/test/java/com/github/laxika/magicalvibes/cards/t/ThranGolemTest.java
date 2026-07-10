package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThranGolemTest extends BaseCardTest {

    // ===== Without an aura =====

    @Test
    @DisplayName("Without an aura, is a plain 3/3 with no granted keywords")
    void withoutAura() {
        Permanent golem = addGolem(player1);

        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isFalse();
    }

    // ===== With an aura attached =====

    @Test
    @DisplayName("While enchanted, gets +2/+2 and flying, first strike, trample")
    void whileEnchanted() {
        Permanent golem = addGolem(player1);
        Permanent aura = addAura(player1);
        aura.setAttachedTo(golem.getId());

        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isTrue();
    }

    // ===== Aura on another creature doesn't count =====

    @Test
    @DisplayName("An aura attached to another creature does not enchant the Golem")
    void auraOnOtherCreature() {
        Permanent golem = addGolem(player1);
        Permanent other = addGolem(player1);
        Permanent aura = addAura(player1);
        aura.setAttachedTo(other.getId());

        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isFalse();
    }

    // ===== Aura removed loses the bonus =====

    @Test
    @DisplayName("After the aura is detached, loses the boost and keywords")
    void afterAuraDetached() {
        Permanent golem = addGolem(player1);
        Permanent aura = addAura(player1);
        aura.setAttachedTo(golem.getId());

        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(5);

        aura.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isFalse();
    }

    // ===== Helpers =====

    private Permanent addGolem(Player player) {
        Permanent perm = new Permanent(new ThranGolem());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAura(Player player) {
        Permanent perm = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
