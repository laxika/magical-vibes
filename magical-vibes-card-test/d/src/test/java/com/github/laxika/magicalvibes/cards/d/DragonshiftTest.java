package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dragonshift.class, GrizzlyBears.class, SerraAngel.class, Xenograft.class})
class DragonshiftTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature becomes a 4/4 blue and red Dragon with flying")
    void transformsTargetCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel()); // 4/4 white

        cast(angel.getId());

        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasColor(gd, angel, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.RED)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isFalse();
        assertThat(angel.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Abilities are lost but the granted flying survives")
    void stripsAbilitiesButKeepsFlying() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel()); // flying, vigilance

        cast(angel.getId());

        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Everything wears off at end of turn")
    void wearsOffAtCleanup() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        cast(angel.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.RED)).isFalse();
        assertThat(angel.getTransientCreatureTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void cannotTargetOpponentCreature() {
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dragonshift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID theirId = theirs.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, theirId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Overloaded, it transforms every creature you control and needs no target")
    void overloadTransformsAllOwnCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dragonshift()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, first, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasColor(gd, first, CardColor.RED)).isTrue();
        assertThat(first.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();

        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getTransientCreatureTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Normal casting transforms only the targeted creature")
    void leavesOtherOwnCreaturesUnchanged() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        cast(target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.DRAGON)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.BEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasColor(gd, other, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasColor(gd, other, CardColor.RED)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, other, CardSubtype.ANGEL)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, other, CardSubtype.DRAGON)).isFalse();
    }

    @Test
    @DisplayName("Counters modify the new base power and toughness and survive cleanup")
    void retainsCountersAboveNewBasePowerToughness() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast(bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasColor(gd, bear, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.BEAR)).isTrue();
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Overload does not transform creatures entering after resolution")
    void overloadDoesNotAffectLaterCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dragonshift()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        Permanent later = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, later, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasColor(gd, later, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasColor(gd, later, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasColor(gd, later, CardColor.RED)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, later, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, later, CardSubtype.DRAGON)).isFalse();
    }

    @Test
    @DisplayName("A later Xenograft adds its chosen type to the transformed Dragon")
    void laterTypeGrantSurvivesDragonTypeOverride() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(bear.getId());

        harness.castFromHand(player1, new Xenograft(), "{4}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.DRAGON)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bear, CardSubtype.BEAR)).isFalse();
    }

    private void cast(UUID targetId) {
        harness.setHand(player1, List.of(new Dragonshift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
