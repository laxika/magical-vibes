package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HearthKami;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.k.KikiJikiMirrorBreaker;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The four hosts cover the condition matrix: Kiki-Jiki, Mirror Breaker (legendary + red), Hearth
 * Kami (red only), Isamaru, Hound of Konda (legendary only) and Sakura-Tribe Elder (neither).
 */
@CardUsed({TenzaGodosMaul.class, SakuraTribeElder.class, HearthKami.class,
        IsamaruHoundOfKonda.class, KikiJikiMirrorBreaker.class})
class TenzaGodosMaulTest extends BaseCardTest {

    @Test
    @DisplayName("A creature that is neither legendary nor red only gets +1/+1 and no trample")
    void plainCreatureGetsFlatBoostOnly() {
        Permanent elder = addCreatureReady(player1, new SakuraTribeElder());
        attachMaul(player1, elder);

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elder, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A red creature gets +1/+1 and trample")
    void redCreatureGetsTrample() {
        Permanent kami = addCreatureReady(player1, new HearthKami());
        attachMaul(player1, kami);

        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, kami, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A legendary non-red creature gets +3/+3 but no trample")
    void legendaryCreatureGetsAdditionalBoost() {
        Permanent isamaru = addCreatureReady(player1, new IsamaruHoundOfKonda());
        attachMaul(player1, isamaru);

        assertThat(gqs.getEffectivePower(gd, isamaru)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, isamaru)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, isamaru, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A legendary red creature gets +3/+3 and trample")
    void legendaryRedCreatureGetsBoth() {
        Permanent kikiJiki = addCreatureReady(player1, new KikiJikiMirrorBreaker());
        attachMaul(player1, kikiJiki);

        assertThat(gqs.getEffectivePower(gd, kikiJiki)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, kikiJiki)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, kikiJiki, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An unattached Maul boosts nothing")
    void unattachedMaulBoostsNothing() {
        addMaulReady(player1);
        Permanent elder = addCreatureReady(player1, new SakuraTribeElder());

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elder, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Equip {1} attaches the Maul to a creature you control")
    void equipAttachesToCreature() {
        Permanent maul = addMaulReady(player1);
        Permanent kami = addCreatureReady(player1, new HearthKami());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, kami.getId());
        harness.passBothPriorities();

        assertThat(maul.getAttachedTo()).isEqualTo(kami.getId());
        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip cannot target a creature an opponent controls")
    void equipRejectsOpponentCreature() {
        Permanent maul = addMaulReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new SakuraTribeElder());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
        assertThat(maul.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Boosts end when the Maul leaves the battlefield")
    void boostEndsWhenMaulLeaves() {
        Permanent kikiJiki = addCreatureReady(player1, new KikiJikiMirrorBreaker());
        Permanent maul = attachMaul(player1, kikiJiki);

        assertThat(gqs.getEffectivePower(gd, kikiJiki)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(maul);

        assertThat(gqs.getEffectivePower(gd, kikiJiki)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, kikiJiki, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addMaulReady(Player player) {
        Permanent perm = new Permanent(new TenzaGodosMaul());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent attachMaul(Player player, Permanent host) {
        Permanent maul = addMaulReady(player);
        maul.setAttachedTo(host.getId());
        return maul;
    }
}
