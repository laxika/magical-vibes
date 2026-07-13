package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RunesOfTheDeusTest extends BaseCardTest {

    private Permanent attach(Permanent creature) {
        Permanent runes = new Permanent(new RunesOfTheDeus());
        runes.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(runes);
        return runes;
    }

    // ===== Red enchanted creature =====

    @Test
    @DisplayName("Red enchanted creature gets +1/+1 and double strike, no trample")
    void redCreatureGetsDoubleStrike() {
        Permanent red = new Permanent(new HillGiant());
        red.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(red);
        attach(red);

        // Hill Giant is 3/3, with +1/+1 should be 4/4
        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, red)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, red, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, red, Keyword.TRAMPLE)).isFalse();
    }

    // ===== Green enchanted creature =====

    @Test
    @DisplayName("Green enchanted creature gets +1/+1 and trample, no double strike")
    void greenCreatureGetsTrample() {
        Permanent green = new Permanent(new GrizzlyBears());
        green.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(green);
        attach(green);

        // Grizzly Bears is 2/2, with +1/+1 should be 3/3
        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, green, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, green, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    // ===== Neither red nor green =====

    @Test
    @DisplayName("Non-red, non-green enchanted creature gets no boost or keywords")
    void otherColorCreatureUnaffected() {
        Permanent white = new Permanent(new HonorGuard());
        white.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(white);
        attach(white);

        // Honor Guard is a 1/1 white creature — unaffected
        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, white, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, white, Keyword.TRAMPLE)).isFalse();
    }

    // ===== Removal restores base stats =====

    @Test
    @DisplayName("Boost and keyword wear off when Runes of the Deus is removed")
    void boostRemovedWhenAuraLeaves() {
        Permanent red = new Permanent(new HillGiant());
        red.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(red);
        Permanent runes = attach(red);

        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, red, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(runes);

        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, red, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    // ===== Casting attaches to target =====

    @Test
    @DisplayName("Resolving Runes of the Deus attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent red = new Permanent(new HillGiant());
        red.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(red);

        harness.setHand(player1, List.of(new RunesOfTheDeus()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0, red.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Runes of the Deus")
                        && p.isAttached()
                        && p.getAttachedTo().equals(red.getId()));
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Runes of the Deus")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new RunesOfTheDeus()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
