package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HowlOfTheHunt.class, HowlpackWolf.class, GrizzlyBears.class, FountainOfYouth.class})
class HowlOfTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("A Wolf is untapped, boosted, and gains vigilance")
    void wolfIsUntappedBoostedAndGainsVigilance() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new HowlpackWolf());
        wolf.tap();

        castHowl(wolf);

        assertThat(wolf.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A non-Wolf creature keeps its tapped state but still gets the Aura's static effects")
    void nonWolfIsNotUntapped() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();

        castHowl(bears);

        assertThat(bears.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Aura's static effects stop when it leaves the battlefield")
    void staticEffectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HowlOfTheHunt());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Howl of the Hunt cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new HowlOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castHowl(Permanent target) {
        harness.setHand(player1, List.of(new HowlOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
