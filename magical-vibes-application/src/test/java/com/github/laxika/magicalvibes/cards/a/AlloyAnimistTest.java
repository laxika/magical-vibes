package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.s.ScrapworkMutt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlloyAnimist.class, EnergyRefractor.class, ScrapworkMutt.class})
class AlloyAnimistTest extends BaseCardTest {

    @Test
    @DisplayName("Animates a noncreature artifact you control into a 4/4 artifact creature")
    void animatesControlledArtifact() {
        addCreatureReady(player1, new AlloyAnimist());
        Permanent refractor = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, refractor.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, refractor)).isTrue();
        assertThat(gqs.isArtifact(refractor)).isTrue();
        assertThat(refractor.getEffectivePower()).isEqualTo(4);
        assertThat(refractor.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("The animation wears off at end of turn")
    void animationEndsAtEndOfTurn() {
        addCreatureReady(player1, new AlloyAnimist());
        Permanent refractor = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, refractor.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, refractor)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, refractor)).isFalse();
        assertThat(gqs.isArtifact(refractor)).isTrue();
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        addCreatureReady(player1, new AlloyAnimist());
        Permanent mutt = harness.addToBattlefieldAndReturn(player1, new ScrapworkMutt());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mutt.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact you control");
    }

    @Test
    @DisplayName("Cannot target an opponent's noncreature artifact")
    void cannotTargetOpponentsArtifact() {
        addCreatureReady(player1, new AlloyAnimist());
        Permanent refractor = harness.addToBattlefieldAndReturn(player2, new EnergyRefractor());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, refractor.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact you control");
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        Permanent animist = addCreatureReady(player1, new AlloyAnimist());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, animist.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact you control");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Animist can activate without tapping")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent animist = harness.addToBattlefieldAndReturn(player1, new AlloyAnimist());
        animist.setSummoningSick(true);
        animist.setTapped(true);
        Permanent refractor = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, refractor.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, refractor)).isTrue();
        assertThat(refractor.getEffectivePower()).isEqualTo(4);
        assertThat(refractor.getEffectiveToughness()).isEqualTo(4);
        assertThat(animist.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The same Animist can animate two artifacts in a turn")
    void canActivateRepeatedly() {
        Permanent animist = addCreatureReady(player1, new AlloyAnimist());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, first)).isTrue();
        assertThat(gqs.isCreature(gd, second)).isTrue();
        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(animist.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An artifact that changes controller before resolution is not animated")
    void targetMustRemainControlledAtResolution() {
        addCreatureReady(player1, new AlloyAnimist());
        Permanent refractor = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, refractor.getId());

        gd.playerBattlefields.get(player1.getId()).remove(refractor);
        gd.playerBattlefields.get(player2.getId()).add(refractor);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, refractor)).isFalse();
        assertThat(gqs.isArtifact(refractor)).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A pending animation fizzles if another activation makes its target a creature")
    void targetMustRemainNoncreatureAtResolution() {
        addCreatureReady(player1, new AlloyAnimist());
        Permanent refractor = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, refractor.getId());
        harness.activateAbility(player1, 0, null, refractor.getId());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, refractor)).isTrue();
        assertThat(refractor.getEffectivePower()).isEqualTo(4);
        assertThat(refractor.getEffectiveToughness()).isEqualTo(4);
        assertThat(gameLogContains("fizzles (illegal target)")).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An activation resolves after its source leaves the battlefield")
    void animationDoesNotDependOnSourceRemaining() {
        Permanent animist = addCreatureReady(player1, new AlloyAnimist());
        Permanent refractor = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, refractor.getId());

        gd.playerBattlefields.get(player1.getId()).remove(animist);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, refractor)).isTrue();
        assertThat(gqs.isArtifact(refractor)).isTrue();
        assertThat(refractor.getEffectivePower()).isEqualTo(4);
        assertThat(refractor.getEffectiveToughness()).isEqualTo(4);
    }
}
