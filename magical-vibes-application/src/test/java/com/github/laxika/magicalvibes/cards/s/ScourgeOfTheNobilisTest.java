package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScourgeOfTheNobilisTest extends BaseCardTest {

    private Permanent attachTo(Permanent creature) {
        Permanent aura = new Permanent(new ScourgeOfTheNobilis());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    // ===== Red: +1/+1 and firebreathing ability =====

    @Test
    @DisplayName("Red enchanted creature gets +1/+1")
    void redGetsBoost() {
        Permanent giant = new Permanent(new HillGiant()); // 3/3 red
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(giant);
        attachTo(giant);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Red enchanted creature can pay {R/W} to pump itself +1/+0")
    void redGrantsFirebreathing() {
        Permanent giant = new Permanent(new HillGiant()); // 3/3 red
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(giant);
        attachTo(giant);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // 3 base +1 (red boost) +1 (pump) = 5
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    // ===== White: +1/+1 and lifelink =====

    @Test
    @DisplayName("White enchanted creature gets +1/+1 and lifelink")
    void whiteGetsBoostAndLifelink() {
        Permanent vanguard = new Permanent(new EliteVanguard()); // 2/1 white
        vanguard.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(vanguard);
        attachTo(vanguard);

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.LIFELINK)).isTrue();
    }

    // ===== Off-color: nothing =====

    @Test
    @DisplayName("Non-red non-white enchanted creature gets no boost or lifelink")
    void offColorGetsNothing() {
        Permanent bears = new Permanent(new GrizzlyBears()); // 2/2 green
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        attachTo(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    // ===== Wears off on removal =====

    @Test
    @DisplayName("Boost and lifelink wear off when the aura is removed")
    void wearsOffWhenRemoved() {
        Permanent vanguard = new Permanent(new EliteVanguard()); // 2/1 white
        vanguard.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(vanguard);
        Permanent aura = attachTo(vanguard);

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.LIFELINK)).isFalse();
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Scourge of the Nobilis")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ScourgeOfTheNobilis()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
