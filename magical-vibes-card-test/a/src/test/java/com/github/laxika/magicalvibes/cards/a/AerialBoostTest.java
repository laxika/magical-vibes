package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AerialBoost.class, Forest.class, GrizzlyBears.class, AlabasterHostSanctifier.class})
class AerialBoostTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+2 and gains flying")
    void boostsAndGrantsFlying() {
        Permanent target = castAerialBoost(new GrizzlyBears());

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = castAerialBoost(new GrizzlyBears());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Convoke taps a creature to help cast Aerial Boost")
    void castsWithConvoke() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AerialBoost()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()), List.of(convokeCreature.getId()));

        assertThat(convokeCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new AerialBoost()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void convokesUsingSummoningSickCreaturesIncludingTheTargetWithoutMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        target.setSummoningSick(true);
        helper.setSummoningSick(true);
        harness.setHand(player1, List.of(new AerialBoost()));

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(target.getId(), helper.getId()));

        assertThat(target.isTapped()).isTrue();
        assertThat(helper.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(helper.getEffectivePower()).isEqualTo(2);
        assertThat(helper.getEffectiveToughness()).isEqualTo(2);
        assertThat(helper.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void cannotConvokeTheSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        harness.setHand(player1, List.of(new AerialBoost()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(target.getId()), List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetAnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlabasterHostSanctifier());
        harness.setHand(player1, List.of(new AerialBoost()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void greenCreaturesCannotConvokeTheWhiteManaRequirement() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AerialBoost()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(target.getId()), List.of(target.getId(), helper.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotConvokeAnAlreadyTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        target.tap();
        harness.setHand(player1, List.of(new AerialBoost()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(target.getId()), List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotConvokeAnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlabasterHostSanctifier());
        harness.setHand(player1, List.of(new AerialBoost()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(target.getId()), List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotAffectAnotherCreatureWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new AlabasterHostSanctifier());
        harness.setHand(player1, List.of(new AerialBoost()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        assertThat(other.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof AerialBoost);
    }

    private Permanent castAerialBoost(GrizzlyBears targetCard) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCard);
        harness.setHand(player1, List.of(new AerialBoost()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        return target;
    }
}
