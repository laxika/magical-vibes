package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CatOwl;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({PillarLaunch.class, CatOwl.class, Forest.class})
class PillarLaunchTest extends BaseCardTest {

    @Test
    @DisplayName("Pillar Launch boosts, grants reach to, and untaps the target creature")
    void boostsGrantsReachAndUntapsTarget() {
        Permanent target = addTappedCreature();
        cast(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).contains(Keyword.REACH);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Pillar Launch's boost and reach wear off at cleanup")
    void temporaryEffectsWearOffAtCleanup() {
        Permanent target = addTappedCreature();
        cast(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.REACH);
    }

    @Test
    @DisplayName("Pillar Launch cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new PillarLaunch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Pillar Launch also boosts and grants reach to an untapped creature")
    void affectsUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CatOwl());
        Permanent other = addTappedCreature();

        cast(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).contains(Keyword.REACH);
        assertThat(target.isTapped()).isFalse();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.getGrantedKeywords()).doesNotContain(Keyword.REACH);
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pillar Launch can boost and untap an opponent's creature")
    void affectsOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CatOwl());
        target.tap();

        cast(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).contains(Keyword.REACH);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Pillar Launch has no effect when its only target leaves before resolution")
    void doesNotResolveWhenTargetLeaves() {
        Permanent target = addTappedCreature();
        Permanent other = addTappedCreature();
        harness.setHand(player1, List.of(new PillarLaunch()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());

        target.setMarkedDamage(3);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Pillar Launch");
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.getGrantedKeywords()).doesNotContain(Keyword.REACH);
        assertThat(other.isTapped()).isTrue();
    }

    private Permanent addTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CatOwl());
        target.tap();
        return target;
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new PillarLaunch()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
