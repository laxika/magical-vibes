package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hollowsage;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EriettesTemptingApple.class, Forest.class, GrizzlyBears.class, Hollowsage.class})
class EriettesTemptingAppleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by untapping, stealing, and granting haste to a target creature until end of turn")
    void entersAndStealsCreatureUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, java.util.List.of(new EriettesTemptingApple()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("The enter-the-battlefield ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, java.util.List.of(new EriettesTemptingApple()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Sacrificing the Apple gains 3 life")
    void sacrificesToGainLife() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new EriettesTemptingApple());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        harness.assertInGraveyard(player1, "Eriette's Tempting Apple");
    }

    @Test
    @DisplayName("Sacrificing the Apple makes a target opponent lose 3 life")
    void sacrificesToMakeOpponentLoseLife() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new EriettesTemptingApple());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Eriette's Tempting Apple");
    }

    @Test
    @CardUsed({EriettesTemptingApple.class, Hollowsage.class})
    @DisplayName("Control changes before untapping, so the new controller controls the untap trigger")
    void newControllerControlsUntapTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Hollowsage());
        target.tap();
        harness.setHand(player1, java.util.List.of(new EriettesTemptingApple()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("The life-loss ability cannot target its controller")
    void cannotTargetControllerWithLifeLoss() {
        harness.addToBattlefield(player1, new EriettesTemptingApple());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Eriette's Tempting Apple");
        harness.assertNotInGraveyard(player1, "Eriette's Tempting Apple");
    }

    @Test
    @DisplayName("Neither activated ability can pay its tap cost while the Apple is tapped")
    void tappedAppleCannotActivateEitherAbility() {
        Permanent apple = harness.addToBattlefieldAndReturn(player1, new EriettesTemptingApple());
        apple.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Eriette's Tempting Apple");
        harness.assertNotInGraveyard(player1, "Eriette's Tempting Apple");
    }

    @Test
    @DisplayName("Sacrificing the Apple does not end its control or haste effects early")
    void sacrificingAppleDoesNotEndTemporaryEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, java.util.List.of(new EriettesTemptingApple()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, target.getId());
        resolveAllTriggers();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertInGraveyard(player1, "Eriette's Tempting Apple");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

}
