package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SpareDagger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheBlackstaffOfWaterdeep.class, SpareDagger.class})
class TheBlackstaffOfWaterdeepTest extends BaseCardTest {

    @Test
    @DisplayName("Animates another nontoken artifact into a 4/4 artifact creature")
    void animatesAnotherArtifact() {
        Permanent staff = addReadyStaff(player1);
        Permanent dagger = addReadySpareDagger(player1);
        addMana();

        harness.activateAbility(player1, 0, null, dagger.getId());
        harness.passBothPriorities();

        assertThat(staff.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, dagger)).isTrue();
        assertThat(gqs.isArtifact(gd, dagger)).isTrue();
        assertThat(gqs.getEffectivePower(gd, dagger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dagger)).isEqualTo(4);
    }

    @Test
    @DisplayName("Animation lasts while The Blackstaff remains tapped")
    void animationLastsWhileTapped() {
        addReadyStaff(player1);
        Permanent dagger = addReadySpareDagger(player1);
        addMana();

        harness.activateAbility(player1, 0, null, dagger.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, dagger)).isTrue();
    }

    @Test
    @DisplayName("Animation ends when The Blackstaff becomes untapped")
    void animationEndsWhenUntapped() {
        Permanent staff = addReadyStaff(player1);
        Permanent dagger = addReadySpareDagger(player1);
        addMana();

        harness.activateAbility(player1, 0, null, dagger.getId());
        harness.passBothPriorities();
        advanceToUntapChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(staff.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, dagger)).isFalse();
    }

    @Test
    @DisplayName("Cannot target The Blackstaff itself")
    void cannotTargetItself() {
        addReadyStaff(player1);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                findPermanent(player1, "The Blackstaff of Waterdeep").getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nontoken artifact you control");
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        addReadyStaff(player1);
        Permanent dagger = addReadySpareDagger(player1);
        addMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dagger.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Declining to untap preserves the animation")
    void decliningToUntapPreservesAnimation() {
        Permanent staff = addReadyStaff(player1);
        Permanent dagger = addReadySpareDagger(player1);
        addMana();

        harness.activateAbility(player1, 0, null, dagger.getId());
        harness.passBothPriorities();
        advanceToUntapChoice();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(staff.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, dagger)).isTrue();
        assertThat(gqs.getEffectivePower(gd, dagger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dagger)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target an opponent's artifact")
    void cannotTargetOpponentsArtifact() {
        addReadyStaff(player1);
        Permanent dagger = addReadySpareDagger(player2);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dagger.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact token")
    void cannotTargetArtifactToken() {
        addReadyStaff(player1);
        SpareDagger token = new SpareDagger();
        token.setToken(true);
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, token);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dagger.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nontoken artifact you control");
    }

    @Test
    @DisplayName("Animation does not begin if the source untaps before resolution")
    void untappingBeforeResolutionPreventsAnimation() {
        Permanent staff = addReadyStaff(player1);
        Permanent dagger = addReadySpareDagger(player1);
        addMana();

        harness.activateAbility(player1, 0, null, dagger.getId());
        staff.untap();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, dagger)).isFalse();
    }

    @Test
    @DisplayName("Untapping and retapping before resolution does not restart the duration")
    void untappingAndRetappingBeforeResolutionPreventsAnimation() {
        Permanent staff = addReadyStaff(player1);
        Permanent dagger = addReadySpareDagger(player1);
        addMana();

        harness.activateAbility(player1, 0, null, dagger.getId());
        staff.untap();
        staff.tap();
        harness.passBothPriorities();

        assertThat(staff.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, dagger)).isFalse();
    }

    @Test
    @DisplayName("Animation does not begin if the source leaves before resolution")
    void sourceLeavingBeforeResolutionPreventsAnimation() {
        Permanent staff = addReadyStaff(player1);
        Permanent dagger = addReadySpareDagger(player1);
        addMana();

        harness.activateAbility(player1, 0, null, dagger.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, staff));
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, dagger)).isFalse();
    }

    @Test
    @DisplayName("Animation ends when the source leaves the battlefield")
    void sourceLeavingEndsAnimation() {
        Permanent staff = addReadyStaff(player1);
        Permanent dagger = addReadySpareDagger(player1);
        addMana();

        harness.activateAbility(player1, 0, null, dagger.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, staff));

        assertThat(gqs.isCreature(gd, dagger)).isFalse();
        assertThat(gqs.isArtifact(gd, dagger)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate outside a main phase")
    void cannotActivateDuringCombat() {
        addReadyStaff(player1);
        Permanent dagger = addReadySpareDagger(player1);
        addMana();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dagger.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadyStaff(Player player) {
        return addCreatureReady(player, new TheBlackstaffOfWaterdeep());
    }

    private Permanent addReadySpareDagger(Player player) {
        return addCreatureReady(player, new SpareDagger());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void advanceToUntapChoice() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UNTAP);
    }

}
