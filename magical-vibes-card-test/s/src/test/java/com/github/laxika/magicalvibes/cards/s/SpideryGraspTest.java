package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpideryGrasp.class, DarkthicketWolf.class, Forest.class})
class SpideryGraspTest extends BaseCardTest {


    @Test
    @DisplayName("Resolving Spidery Grasp untaps, boosts, and grants reach to target creature")
    void untapsBoostsAndGrantsReach() {
        Permanent target = addTappedCreature(player2);
        harness.setHand(player1, List.of(new SpideryGrasp()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Boost and reach expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = addTappedCreature(player2);
        harness.setHand(player1, List.of(new SpideryGrasp()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        Permanent ownCreature = addTappedCreature(player1);
        harness.setHand(player1, List.of(new SpideryGrasp()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, ownCreature.getId());

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(ownCreature.getPowerModifier()).isEqualTo(2);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(4);
        assertThat(ownCreature.hasKeyword(Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SpideryGrasp()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Spidery Grasp fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addTappedCreature(player2);
        harness.setHand(player1, List.of(new SpideryGrasp()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("An untapped creature still gets the boost and reach")
    void boostsAlreadyUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DarkthicketWolf());
        harness.setHand(player1, List.of(new SpideryGrasp()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Repeated casts stack their boosts and do not affect other creatures")
    void repeatedCastsStackOnlyOnTarget() {
        Permanent target = addTappedCreature(player1);
        Permanent other = addTappedCreature(player2);
        harness.setHand(player1, List.of(new SpideryGrasp(), new SpideryGrasp()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(8);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.REACH)).isFalse();
    }

    private Permanent addTappedCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DarkthicketWolf());
        perm.tap();
        return perm;
    }
}
