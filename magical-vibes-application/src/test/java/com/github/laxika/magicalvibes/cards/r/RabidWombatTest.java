package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RabidWombat.class, HolyStrength.class, LeoninScimitar.class})
class RabidWombatTest extends BaseCardTest {

    @Test
    @DisplayName("Without attachments, is base 0/1")
    void withoutAttachments() {
        Permanent wombat = addCreatureReady(player1, new RabidWombat());

        assertThat(gqs.getEffectivePower(gd, wombat)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, wombat)).isEqualTo(1);
    }

    @Test
    @DisplayName("With one Aura attached, gets +2/+2 plus the Aura's stats")
    void withOneAura() {
        Permanent wombat = addCreatureReady(player1, new RabidWombat());
        Permanent aura = addCreatureReady(player1, new HolyStrength());
        aura.setAttachedTo(wombat.getId());

        // Base 0/1 + 2/2 from ability + 1/2 from Holy Strength = 3/5
        assertThat(gqs.getEffectivePower(gd, wombat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wombat)).isEqualTo(5);
    }

    @Test
    @DisplayName("With two Auras attached, gets +4/+4")
    void withTwoAuras() {
        Permanent wombat = addCreatureReady(player1, new RabidWombat());
        Permanent aura1 = addCreatureReady(player1, new HolyStrength());
        Permanent aura2 = addCreatureReady(player1, new HolyStrength());
        aura1.setAttachedTo(wombat.getId());
        aura2.setAttachedTo(wombat.getId());

        // Base 0/1 + 4/4 from ability (2 Auras) + 1/2 + 1/2 from Holy Strengths = 6/9
        assertThat(gqs.getEffectivePower(gd, wombat)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, wombat)).isEqualTo(9);
    }

    @Test
    @DisplayName("Attached Equipment does not count toward the bonus")
    void equipmentDoesNotCount() {
        Permanent wombat = addCreatureReady(player1, new RabidWombat());
        Permanent scimitar = addCreatureReady(player1, new LeoninScimitar());
        scimitar.setAttachedTo(wombat.getId());

        // Base 0/1 + 1/1 from Leonin Scimitar only (no ability bonus for Equipment) = 1/2
        assertThat(gqs.getEffectivePower(gd, wombat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wombat)).isEqualTo(2);
    }

    @Test
    @DisplayName("Aura on another creature doesn't count")
    void auraOnOtherCreatureDoesNotCount() {
        Permanent wombat = addCreatureReady(player1, new RabidWombat());
        Permanent other = addCreatureReady(player1, new RabidWombat());
        Permanent aura = addCreatureReady(player1, new HolyStrength());
        aura.setAttachedTo(other.getId());

        assertThat(gqs.getEffectivePower(gd, wombat)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, wombat)).isEqualTo(1);
    }

    @Test
    @DisplayName("An Aura controlled by an opponent still counts when attached")
    void opponentControlledAuraCounts() {
        Permanent wombat = addCreatureReady(player1, new RabidWombat());
        Permanent aura = addCreatureReady(player2, new HolyStrength());
        aura.setAttachedTo(wombat.getId());

        assertThat(gqs.getEffectivePower(gd, wombat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wombat)).isEqualTo(5);
    }

}
