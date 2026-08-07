package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamahlPitFighter;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The three hosts cover the condition matrix: Kamahl, Pit Fighter (legendary + red), Raging Goblin
 * (red only), Arvad the Cursed (legendary only) and Grizzly Bears (neither).
 */
class TenzaGodosMaulTest extends BaseCardTest {

    @Test
    @DisplayName("A creature that is neither legendary nor red only gets +1/+1 and no trample")
    void plainCreatureGetsFlatBoostOnly() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachMaul(player1, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A red creature gets +1/+1 and trample")
    void redCreatureGetsTrample() {
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        attachMaul(player1, goblin);

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A legendary non-red creature gets +3/+3 but no trample")
    void legendaryCreatureGetsAdditionalBoost() {
        Permanent arvad = addCreatureReady(player1, new ArvadTheCursed());
        attachMaul(player1, arvad);

        assertThat(gqs.getEffectivePower(gd, arvad)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, arvad)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, arvad, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A legendary red creature gets +3/+3 and trample")
    void legendaryRedCreatureGetsBoth() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        attachMaul(player1, kamahl);

        assertThat(gqs.getEffectivePower(gd, kamahl)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, kamahl)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, kamahl, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An unattached Maul boosts nothing")
    void unattachedMaulBoostsNothing() {
        addMaulReady(player1);
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Equip {1} attaches the Maul to a creature you control")
    void equipAttachesToCreature() {
        Permanent maul = addMaulReady(player1);
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(maul.getAttachedTo()).isEqualTo(goblin.getId());
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts end when the Maul leaves the battlefield")
    void boostEndsWhenMaulLeaves() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        Permanent maul = attachMaul(player1, kamahl);

        assertThat(gqs.getEffectivePower(gd, kamahl)).isEqualTo(9);

        gd.playerBattlefields.get(player1.getId()).remove(maul);

        assertThat(gqs.getEffectivePower(gd, kamahl)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, kamahl, Keyword.TRAMPLE)).isFalse();
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
