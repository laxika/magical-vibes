package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.n.NagaoBoundByHonor;
import com.github.laxika.magicalvibes.cards.n.NezumiCutthroat;
import com.github.laxika.magicalvibes.cards.n.NezumiRonin;
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
 * Konda's Banner is equipped to Konda, Lord of Eiganjo (white, Human Samurai) throughout: Isamaru
 * shares only the color, Nezumi Ronin only the creature type, Nagao both, and Nezumi Cutthroat
 * neither.
 */
@CardUsed({KondasBanner.class, KondaLordOfEiganjo.class, IsamaruHoundOfKonda.class,
        NezumiRonin.class, NagaoBoundByHonor.class, NezumiCutthroat.class, KuroPitlord.class})
class KondasBannerTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches the Banner to a legendary creature")
    void equipAttachesToLegendaryCreature() {
        Permanent banner = addBannerReady(player1);
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, konda.getId());
        harness.passBothPriorities();

        assertThat(banner.getAttachedTo()).isEqualTo(konda.getId());
    }

    @Test
    @DisplayName("Equip is rejected when the target creature is not legendary")
    void equipRejectsNonLegendaryCreature() {
        addBannerReady(player1);
        Permanent cutthroat = addCreatureReady(player1, new NezumiCutthroat());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, cutthroat.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("A creature sharing only a color with the equipped creature gets +1/+1")
    void colorShareGivesOneBoost() {
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        Permanent isamaru = addCreatureReady(player1, new IsamaruHoundOfKonda());
        attachBanner(player1, konda);

        assertThat(gqs.getEffectivePower(gd, isamaru)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, isamaru)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature sharing only a creature type with the equipped creature gets +1/+1")
    void creatureTypeShareGivesOneBoost() {
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        Permanent ronin = addCreatureReady(player1, new NezumiRonin());
        attachBanner(player1, konda);

        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ronin)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature sharing both a color and a creature type gets +2/+2")
    void bothSharesStack() {
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        Permanent nagao = addCreatureReady(player1, new NagaoBoundByHonor());
        attachBanner(player1, konda);

        assertThat(gqs.getEffectivePower(gd, nagao)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, nagao)).isEqualTo(5);
    }

    @Test
    @DisplayName("A creature sharing neither is unaffected")
    void noShareGivesNoBoost() {
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        Permanent cutthroat = addCreatureReady(player1, new NezumiCutthroat());
        attachBanner(player1, konda);

        assertThat(gqs.getEffectivePower(gd, cutthroat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cutthroat)).isEqualTo(1);
    }

    @Test
    @DisplayName("The equipped creature shares its own color and type, so it gets +2/+2")
    void equippedCreatureBoostsItself() {
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        attachBanner(player1, konda);

        assertThat(gqs.getEffectivePower(gd, konda)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, konda)).isEqualTo(5);
    }

    @Test
    @DisplayName("Creatures an opponent controls are boosted too")
    void boostsOpponentCreatures() {
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        Permanent opponentIsamaru = addCreatureReady(player2, new IsamaruHoundOfKonda());
        attachBanner(player1, konda);

        assertThat(gqs.getEffectivePower(gd, opponentIsamaru)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentIsamaru)).isEqualTo(3);
    }

    @Test
    @DisplayName("An unattached Banner boosts nothing")
    void unattachedBannerBoostsNothing() {
        addBannerReady(player1);
        Permanent isamaru = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());

        assertThat(gqs.getEffectivePower(gd, isamaru)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, konda)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boosts end when the Banner leaves the battlefield")
    void boostEndsWhenBannerLeaves() {
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        Permanent isamaru = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent banner = attachBanner(player1, konda);

        assertThat(gqs.getEffectivePower(gd, isamaru)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, isamaru)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(banner);

        assertThat(gqs.getEffectivePower(gd, isamaru)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, isamaru)).isEqualTo(2);
    }

    @Test
    @DisplayName("Re-equipping the Banner changes the colors and types it compares")
    void reequippingChangesComparisonCreature() {
        Permanent banner = addBannerReady(player1);
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        Permanent isamaru = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent ronin = addCreatureReady(player1, new NezumiRonin());
        Permanent kuro = addCreatureReady(player1, new KuroPitlord());
        banner.setAttachedTo(konda.getId());

        assertThat(gqs.getEffectivePower(gd, isamaru)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, kuro)).isEqualTo(9);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, kuro.getId());
        harness.passBothPriorities();

        assertThat(banner.getAttachedTo()).isEqualTo(kuro.getId());
        assertThat(gqs.getEffectivePower(gd, isamaru)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, konda)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, kuro)).isEqualTo(11);
    }

    @Test
    @DisplayName("The Banner falls off a creature that stops being legendary")
    void illegalAttachmentFallsOff() {
        Permanent banner = addBannerReady(player1);
        Permanent cutthroat = addCreatureReady(player1, new NezumiCutthroat());
        banner.setAttachedTo(cutthroat.getId());

        harness.runStateBasedActions();

        assertThat(banner.getAttachedTo()).isNull();
    }

    private Permanent addBannerReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new KondasBanner());
    }

    private Permanent attachBanner(Player player, Permanent host) {
        Permanent banner = addBannerReady(player);
        banner.setAttachedTo(host.getId());
        return banner;
    }
}
