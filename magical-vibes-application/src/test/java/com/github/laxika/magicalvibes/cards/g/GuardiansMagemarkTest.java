package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardiansMagemark.class, FountainOfYouth.class, GrizzlyBears.class})
class GuardiansMagemarkTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Guardian's Magemark attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new GuardiansMagemark()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GuardiansMagemark
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Guardian's Magemark boosts enchanted creatures you control")
    void boostsEnchantedCreaturesYouControl() {
        Permanent enchantedBears = new Permanent(new GrizzlyBears());
        Permanent unenchantedBears = new Permanent(new GrizzlyBears());
        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(enchantedBears);
        gd.playerBattlefields.get(player1.getId()).add(unenchantedBears);
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);

        Permanent magemark = new Permanent(new GuardiansMagemark());
        magemark.setAttachedTo(enchantedBears.getId());
        gd.playerBattlefields.get(player1.getId()).add(magemark);

        assertThat(gqs.getEffectivePower(gd, enchantedBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantedBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, unenchantedBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unenchantedBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Guardian's Magemark stops boosting creatures when it leaves the battlefield")
    void bonusStopsWhenRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        Permanent magemark = new Permanent(new GuardiansMagemark());
        magemark.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(magemark);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(magemark);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Guardian's Magemark fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new GuardiansMagemark()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof GuardiansMagemark);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof GuardiansMagemark);
    }

    @Test
    @DisplayName("Guardian's Magemark cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player1.getId()).add(artifact);
        harness.setHand(player1, List.of(new GuardiansMagemark()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
