package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InkEyesServantOfOniTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage lets the controller reanimate a creature from the damaged player's graveyard")
    void combatDamageReanimatesChosenCreature() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        attackWithInkEyesDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Choosing no card declines the optional reanimation")
    void decliningLeavesCreatureInGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        attackWithInkEyesDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Only creature cards from the damaged player's graveyard are offered")
    void controllerOwnGraveyardIsNotOffered() {
        Card ownBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownBears));
        harness.setGraveyard(player2, List.of());
        attackWithInkEyesDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(ownBears.getId()));
    }

    @Test
    @DisplayName("{1}{B} grants Ink-Eyes a regeneration shield")
    void regenerationShieldIsGranted() {
        Permanent inkEyes = addCreatureReady(player1, new InkEyesServantOfOni());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(inkEyes.getRegenerationShield()).isEqualTo(1);
    }

    private void attackWithInkEyesDealingDamage() {
        Permanent inkEyes = addCreatureReady(player1, new InkEyesServantOfOni());
        inkEyes.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
