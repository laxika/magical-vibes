package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.v.VulshokMorningstar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThranGolem.class, Pacifism.class, VulshokMorningstar.class})
class ThranGolemTest extends BaseCardTest {

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

    @Test
    @DisplayName("An Aura controlled by an opponent still enchants the Golem")
    void auraControlledByOpponentStillEnchants() {
        Permanent golem = addGolem(player1);
        Permanent aura = addAura(player2);
        aura.setAttachedTo(golem.getId());

        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isTrue();
    }

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

    @Test
    @DisplayName("Multiple Auras grant the bonus only once, until the last Aura leaves")
    void multipleAurasDoNotStackAndLastAuraMustLeave() {
        Permanent golem = addGolem(player1);
        Permanent firstAura = addAura(player1);
        Permanent secondAura = addAura(player2);
        firstAura.setAttachedTo(golem.getId());
        secondAura.setAttachedTo(golem.getId());

        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(firstAura);

        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player2.getId()).remove(secondAura);

        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Equipment does not activate the enchanted bonus")
    void equipmentDoesNotEnchant() {
        Permanent golem = addGolem(player1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        equipment.setAttachedTo(golem.getId());

        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addGolem(Player player) {
        return addCreatureReady(player, new ThranGolem());
    }

    private Permanent addAura(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Pacifism());
    }
}
