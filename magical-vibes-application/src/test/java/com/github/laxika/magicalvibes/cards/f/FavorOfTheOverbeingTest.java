package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.w.WistfulSelkie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FavorOfTheOverbeing.class, EliteVanguard.class, FugitiveWizard.class, FountainOfYouth.class, GrizzlyBears.class, WistfulSelkie.class})
class FavorOfTheOverbeingTest extends BaseCardTest {

    private Permanent attach(Permanent creature) {
        Permanent favor = harness.addToBattlefieldAndReturn(player1, new FavorOfTheOverbeing());
        favor.setAttachedTo(creature.getId());
        return favor;
    }

    @Test
    @DisplayName("Green enchanted creature gets +1/+1 and vigilance, but not flying")
    void greenCreatureGetsBoostAndVigilance() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attach(bears);

        // Grizzly Bears is 2/2 -> 3/3
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Blue enchanted creature gets +1/+1 and flying, but not vigilance")
    void blueCreatureGetsBoostAndFlying() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        attach(wizard);

        // Fugitive Wizard is 1/1 -> 2/2
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Non-green, non-blue enchanted creature gets no boost or keywords")
    void neutralCreatureGetsNothing() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        attach(vanguard);

        // Elite Vanguard is 2/1 (white) -> unchanged
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Green creature reverts when Favor of the Overbeing is removed")
    void boostRevertsAfterRemoval() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent favor = attach(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(favor);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new FavorOfTheOverbeing()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Both color bonuses apply after enchanting an opponent's green-blue creature")
    void bothBonusesApplyToOpposingGreenBlueCreature() {
        Permanent selkie = harness.addToBattlefieldAndReturn(player2, new WistfulSelkie());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new WistfulSelkie());
        harness.setHand(player1, List.of(new FavorOfTheOverbeing()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, selkie.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Favor of the Overbeing").getAttachedTo()).isEqualTo(selkie.getId());
        assertThat(gqs.getEffectivePower(gd, selkie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, selkie)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, selkie, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, selkie, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Multiple Favors each grant both bonuses to a green-blue creature")
    void multipleAurasStackBothBonuses() {
        Permanent selkie = harness.addToBattlefieldAndReturn(player1, new WistfulSelkie());
        Permanent first = attach(selkie);
        attach(selkie);

        assertThat(gqs.getEffectivePower(gd, selkie)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, selkie)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, selkie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, selkie)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, selkie, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, selkie, Keyword.FLYING)).isTrue();
    }
}
