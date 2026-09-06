package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
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
 * Konda's Banner is equipped to Kamahl, Pit Fighter (red, Human Barbarian) throughout: Raging
 * Goblin shares only the color, Samite Healer only the creature type, Balduvian
 * Barbarians both, and Grizzly Bears neither.
 */
@CardUsed({KondasBanner.class, KamahlPitFighter.class, BalduvianBarbarians.class, GrizzlyBears.class,
        RagingGoblin.class, SamiteHealer.class})
class KondasBannerTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches the Banner to a legendary creature")
    void equipAttachesToLegendaryCreature() {
        Permanent banner = addBannerReady(player1);
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, kamahl.getId());
        harness.passBothPriorities();

        assertThat(banner.getAttachedTo()).isEqualTo(kamahl.getId());
    }

    @Test
    @DisplayName("Equip is rejected when the target creature is not legendary")
    void equipRejectsNonLegendaryCreature() {
        addBannerReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("A creature sharing only a color with the equipped creature gets +1/+1")
    void colorShareGivesOneBoost() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        attachBanner(player1, kamahl);

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature sharing only a creature type with the equipped creature gets +1/+1")
    void creatureTypeShareGivesOneBoost() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        Permanent healer = addCreatureReady(player1, new SamiteHealer());
        attachBanner(player1, kamahl);

        assertThat(gqs.getEffectivePower(gd, healer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, healer)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature sharing both a color and a creature type gets +2/+2")
    void bothSharesStack() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        Permanent barbarians = addCreatureReady(player1, new BalduvianBarbarians());
        attachBanner(player1, kamahl);

        assertThat(gqs.getEffectivePower(gd, barbarians)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, barbarians)).isEqualTo(4);
    }

    @Test
    @DisplayName("A creature sharing neither is unaffected")
    void noShareGivesNoBoost() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachBanner(player1, kamahl);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The equipped creature shares its own color and type, so it gets +2/+2")
    void equippedCreatureBoostsItself() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        attachBanner(player1, kamahl);

        assertThat(gqs.getEffectivePower(gd, kamahl)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, kamahl)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures an opponent controls are boosted too")
    void boostsOpponentCreatures() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        Permanent opponentGoblin = addCreatureReady(player2, new RagingGoblin());
        attachBanner(player1, kamahl);

        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("An unattached Banner boosts nothing")
    void unattachedBannerBoostsNothing() {
        addBannerReady(player1);
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, kamahl)).isEqualTo(6);
    }

    @Test
    @DisplayName("Boosts end when the Banner leaves the battlefield")
    void boostEndsWhenBannerLeaves() {
        Permanent kamahl = addCreatureReady(player1, new KamahlPitFighter());
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        Permanent banner = attachBanner(player1, kamahl);

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(banner);

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
    }

    private Permanent addBannerReady(Player player) {
        Permanent perm = new Permanent(new KondasBanner());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent attachBanner(Player player, Permanent host) {
        Permanent banner = addBannerReady(player);
        banner.setAttachedTo(host.getId());
        return banner;
    }
}
