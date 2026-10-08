package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.s.ScaledWurm;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhaleboneGlider.class, BalduvianBears.class, BalduvianBarbarians.class, ScaledWurm.class})
class WhaleboneGliderTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants flying to a creature with power 3 or less")
    void grantsFlyingToSmallCreature() {
        addCreatureReady(player1, new WhaleboneGlider());
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps the Glider")
    void activatingTapsSelf() {
        Permanent glider = addCreatureReady(player1, new WhaleboneGlider());
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(glider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOff() {
        addCreatureReady(player1, new WhaleboneGlider());
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 3")
    void cannotTargetBigCreature() {
        addCreatureReady(player1, new WhaleboneGlider());
        Permanent giant = addCreatureReady(player2, new ScaledWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target an opponent's creature with power exactly 3")
    void targetsOpponentCreatureAtPowerLimit() {
        addCreatureReady(player1, new WhaleboneGlider());
        Permanent target = addCreatureReady(player2, new BalduvianBarbarians());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new WhaleboneGlider());
        Permanent artifact = addCreatureReady(player2, new WhaleboneGlider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target that grows beyond power 3 becomes illegal before resolution")
    void targetBecomesTooPowerfulBeforeResolution() {
        addCreatureReady(player1, new WhaleboneGlider());
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setPowerModifier(2);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without paying two mana")
    void cannotActivateWithInsufficientMana() {
        Permanent glider = addCreatureReady(player1, new WhaleboneGlider());
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(glider.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while the Glider is tapped")
    void cannotActivateWhileTapped() {
        Permanent glider = addCreatureReady(player1, new WhaleboneGlider());
        glider.setTapped(true);
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A noncreature Glider can activate the turn it enters")
    void canActivateNewlyEnteredArtifact() {
        harness.addToBattlefield(player1, new WhaleboneGlider());
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying remains if the creature grows beyond power 3 after resolution")
    void flyingRemainsAfterCreatureGrows() {
        addCreatureReady(player1, new WhaleboneGlider());
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        target.setPowerModifier(2);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The ability resolves even if the Glider leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent glider = addCreatureReady(player1, new WhaleboneGlider());
        Permanent target = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, glider));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }
}
