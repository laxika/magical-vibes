package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Web.class, GrizzlyBears.class, Millstone.class})
class WebTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Web attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Web()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Web")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Web can enchant a creature controlled by an opponent")
    void canEnchantOpponentsCreature() {
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Web()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, opposingBears.getId());
        harness.passBothPriorities();

        Permanent web = findPermanent(player1, "Web");
        assertThat(web.isAttached()).isTrue();
        assertThat(web.getAttachedTo()).isEqualTo(opposingBears.getId());
    }

    @Test
    @DisplayName("Enchanted creature gets +0/+2 and reach")
    void enchantedCreatureGetsBoostAndReach() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBears = addCreatureReady(player1, new GrizzlyBears());

        Permanent webPerm = harness.addToBattlefieldAndReturn(player1, new Web());
        webPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.REACH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Creature loses boost and reach when Web is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent webPerm = harness.addToBattlefieldAndReturn(player1, new Web());
        webPerm.setAttachedTo(bearsPerm.getId());

        gd.playerBattlefields.get(player1.getId()).remove(webPerm);

        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Web")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.setHand(player1, List.of(new Web()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Web grants its toughness bonus and reach to an opponent's creature")
    void bonusesApplyToOpponentsCreature() {
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Web()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, opposingBears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.REACH)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Multiple Webs stack their toughness bonuses on the same creature")
    void multipleWebsStack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Web(), new Web()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Web")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isTrue();
    }
}
