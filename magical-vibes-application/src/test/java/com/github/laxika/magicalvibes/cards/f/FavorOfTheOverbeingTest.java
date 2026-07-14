package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FavorOfTheOverbeingTest extends BaseCardTest {

    private Permanent attach(Permanent creature) {
        Permanent favor = new Permanent(new FavorOfTheOverbeing());
        favor.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(favor);
        return favor;
    }

    // ===== Green creature: +1/+1 and vigilance =====

    @Test
    @DisplayName("Green enchanted creature gets +1/+1 and vigilance, but not flying")
    void greenCreatureGetsBoostAndVigilance() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        attach(bears);

        // Grizzly Bears is 2/2 -> 3/3
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    // ===== Blue creature: +1/+1 and flying =====

    @Test
    @DisplayName("Blue enchanted creature gets +1/+1 and flying, but not vigilance")
    void blueCreatureGetsBoostAndFlying() {
        Permanent wizard = new Permanent(new FugitiveWizard());
        gd.playerBattlefields.get(player1.getId()).add(wizard);
        attach(wizard);

        // Fugitive Wizard is 1/1 -> 2/2
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Neither green nor blue: nothing =====

    @Test
    @DisplayName("Non-green, non-blue enchanted creature gets no boost or keywords")
    void neutralCreatureGetsNothing() {
        Permanent vanguard = new Permanent(new EliteVanguard());
        gd.playerBattlefields.get(player1.getId()).add(vanguard);
        attach(vanguard);

        // Elite Vanguard is 2/1 (white) -> unchanged
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.FLYING)).isFalse();
    }

    // ===== Boost falls off when the aura leaves =====

    @Test
    @DisplayName("Green creature reverts when Favor of the Overbeing is removed")
    void boostRevertsAfterRemoval() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        Permanent favor = attach(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(favor);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new FavorOfTheOverbeing()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
