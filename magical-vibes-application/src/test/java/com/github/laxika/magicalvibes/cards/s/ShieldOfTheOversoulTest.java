package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RhysTheRedeemed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShieldOfTheOversoulTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Shield of the Oversoul attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent target = new Permanent(new GrizzlyBears());
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new ShieldOfTheOversoul()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Shield of the Oversoul")
                        && p.isAttached()
                        && p.getAttachedTo().equals(target.getId()));
    }

    // ===== Green enchanted creature: +1/+1 and indestructible =====

    @Test
    @DisplayName("Green enchanted creature gets +1/+1 and indestructible, but no flying")
    void greenCreatureGetsBoostAndIndestructible() {
        Permanent green = new Permanent(new GrizzlyBears()); // 2/2 green
        gd.playerBattlefields.get(player1.getId()).add(green);

        Permanent shield = new Permanent(new ShieldOfTheOversoul());
        shield.setAttachedTo(green.getId());
        gd.playerBattlefields.get(player1.getId()).add(shield);

        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, green, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, green, Keyword.FLYING)).isFalse();
    }

    // ===== White enchanted creature: +1/+1 and flying =====

    @Test
    @DisplayName("White enchanted creature gets +1/+1 and flying, but is not indestructible")
    void whiteCreatureGetsBoostAndFlying() {
        Permanent white = new Permanent(new EliteVanguard()); // 2/1 white
        gd.playerBattlefields.get(player1.getId()).add(white);

        Permanent shield = new Permanent(new ShieldOfTheOversoul());
        shield.setAttachedTo(white.getId());
        gd.playerBattlefields.get(player1.getId()).add(shield);

        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, white, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, white, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Green and white enchanted creature: both bonuses stack =====

    @Test
    @DisplayName("A green-and-white enchanted creature gets +2/+2, indestructible, and flying")
    void greenWhiteCreatureGetsBothBonuses() {
        Permanent gw = new Permanent(new RhysTheRedeemed()); // 1/1 green/white
        gd.playerBattlefields.get(player1.getId()).add(gw);

        Permanent shield = new Permanent(new ShieldOfTheOversoul());
        shield.setAttachedTo(gw.getId());
        gd.playerBattlefields.get(player1.getId()).add(shield);

        assertThat(gqs.getEffectivePower(gd, gw)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gw)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, gw, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, gw, Keyword.FLYING)).isTrue();
    }

    // ===== Neither green nor white: no bonuses =====

    @Test
    @DisplayName("A creature that is neither green nor white gets no bonuses")
    void nonGreenNonWhiteGetsNothing() {
        Permanent blue = new Permanent(new FugitiveWizard()); // 1/1 blue
        gd.playerBattlefields.get(player1.getId()).add(blue);

        Permanent shield = new Permanent(new ShieldOfTheOversoul());
        shield.setAttachedTo(blue.getId());
        gd.playerBattlefields.get(player1.getId()).add(shield);

        assertThat(gqs.getEffectivePower(gd, blue)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blue)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, blue, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, blue, Keyword.FLYING)).isFalse();
    }

    // ===== Bonuses fall off when the aura leaves =====

    @Test
    @DisplayName("Bonuses are removed when Shield of the Oversoul leaves the battlefield")
    void bonusesRemovedWhenAuraRemoved() {
        Permanent green = new Permanent(new GrizzlyBears()); // 2/2 green
        gd.playerBattlefields.get(player1.getId()).add(green);

        Permanent shield = new Permanent(new ShieldOfTheOversoul());
        shield.setAttachedTo(green.getId());
        gd.playerBattlefields.get(player1.getId()).add(shield);

        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, green, Keyword.INDESTRUCTIBLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(shield);

        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, green, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Shield of the Oversoul")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ShieldOfTheOversoul()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
