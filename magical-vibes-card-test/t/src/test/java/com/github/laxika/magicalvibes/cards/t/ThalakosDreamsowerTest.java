package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Capsize;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FireWhip;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({ThalakosDreamsower.class, HornedTurtle.class, Forest.class, SoltariFootSoldier.class,
        FireWhip.class, Capsize.class})
class ThalakosDreamsowerTest extends BaseCardTest {

    /** Run the next player's untap step and stop at upkeep. */
    private void advanceToNextTurn(Player currentActivePlayer) {
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        advanceToUpkeep(newActivePlayer);
    }

    /** Same, answering the new active player's may-not-untap prompt with {@code acceptUntap}. */
    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }

    @Test
    @DisplayName("Combat-damage trigger chooses a target creature before resolution")
    void choosesTargetCreatureWhenTriggerIsPutOnStack() {
        Permanent dreamsower = addCreatureReady(player1, new ThalakosDreamsower());
        dreamsower.setAttacking(true);
        Permanent ownCreature = addCreatureReady(player1, new HornedTurtle());
        Permanent enemyCreature = addCreatureReady(player2, new HornedTurtle());
        Permanent enemyLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(dreamsower.getId(), ownCreature.getId(), enemyCreature.getId())
                .doesNotContain(enemyLand.getId());
    }

    @Test
    @DisplayName("The chosen creature becomes tapped")
    void tapsChosenCreature() {
        Permanent dreamsower = addCreatureReady(player1, new ThalakosDreamsower());
        dreamsower.setAttacking(true);
        Permanent enemyCreature = addCreatureReady(player2, new HornedTurtle());

        resolveCombat();
        harness.handlePermanentChosen(player1, enemyCreature.getId());
        harness.passBothPriorities();

        assertThat(enemyCreature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The tapped creature stays tapped while Thalakos Dreamsower remains tapped, and untaps once it untaps")
    void untapLockLastsWhileSourceTapped() {
        Permanent dreamsower = addCreatureReady(player1, new ThalakosDreamsower());
        dreamsower.setAttacking(true);
        dreamsower.tap();
        Permanent enemyCreature = addCreatureReady(player2, new HornedTurtle());

        resolveCombat();
        harness.handlePermanentChosen(player1, enemyCreature.getId());
        harness.passBothPriorities();

        // Player 2's untap step — the locked creature stays tapped.
        advanceToNextTurn(player1);
        assertThat(enemyCreature.isTapped()).isTrue();

        // Player 1's untap step — choose to untap Thalakos Dreamsower, releasing the lock.
        advanceToNextTurnWithMayChoice(player2, true);
        assertThat(dreamsower.isTapped()).isFalse();

        // Player 2's next untap step — the creature untaps now.
        advanceToNextTurn(player1);
        assertThat(enemyCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Controller may choose not to untap Thalakos Dreamsower during their untap step")
    void mayChooseNotToUntap() {
        Permanent dreamsower = addCreatureReady(player1, new ThalakosDreamsower());
        dreamsower.tap();

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(dreamsower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Noncombat damage to an opponent taps and locks the chosen creature")
    void noncombatDamageAlsoTapsAndLocksCreature() {
        Permanent dreamsower = addCreatureReady(player1, new ThalakosDreamsower());
        Permanent enemyCreature = addCreatureReady(player2, new HornedTurtle());
        Permanent whip = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        whip.setAttachedTo(dreamsower.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, enemyCreature.getId());
        harness.passBothPriorities();

        assertThat(enemyCreature.isTapped()).isTrue();
        advanceToNextTurn(player1);
        assertThat(enemyCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Targeting itself prevents Dreamsower from untapping during its untap step")
    void selfTargetPreventsOptionalUntap() {
        Permanent dreamsower = addCreatureReady(player1, new ThalakosDreamsower());
        dreamsower.tap();
        dreamsower.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, dreamsower.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        assertThat(dreamsower.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An already tapped target stays locked when Dreamsower's controller declines to untap")
    void alreadyTappedTargetRemainsLockedAfterDecliningUntap() {
        Permanent dreamsower = addCreatureReady(player1, new ThalakosDreamsower());
        dreamsower.tap();
        dreamsower.setAttacking(true);
        Permanent enemyCreature = addCreatureReady(player2, new HornedTurtle());
        enemyCreature.tap();

        resolveCombat();
        harness.handlePermanentChosen(player1, enemyCreature.getId());
        harness.passBothPriorities();

        advanceToNextTurn(player1);
        assertThat(enemyCreature.isTapped()).isTrue();
        advanceToNextTurnWithMayChoice(player2, false);
        assertThat(dreamsower.isTapped()).isTrue();
        advanceToNextTurn(player1);
        assertThat(enemyCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing Dreamsower before its trigger resolves still taps the target but creates no lock")
    void sourceLeavesBeforeResolution() {
        Permanent dreamsower = addCreatureReady(player1, new ThalakosDreamsower());
        dreamsower.tap();
        dreamsower.setAttacking(true);
        Permanent enemyCreature = addCreatureReady(player2, new HornedTurtle());

        resolveCombat();
        harness.handlePermanentChosen(player1, enemyCreature.getId());
        harness.setHand(player1, List.of(new Capsize()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, dreamsower.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Thalakos Dreamsower");
        harness.passBothPriorities();

        assertThat(enemyCreature.isTapped()).isTrue();
        advanceToNextTurn(player1);
        assertThat(enemyCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Shadow prevents blocking by a creature without shadow")
    void shadowRequiresShadowBlocker() {
        Permanent dreamsower = addCreatureReady(player1, new ThalakosDreamsower());
        dreamsower.setAttacking(true);
        Permanent normalBlocker = addCreatureReady(player2, new HornedTurtle());
        Permanent shadowBlocker = addCreatureReady(player2, new SoltariFootSoldier());

        List<Permanent> defenderBattlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(bls.canBlockAttacker(gd, normalBlocker, dreamsower, defenderBattlefield)).isFalse();
        assertThat(bls.canBlockAttacker(gd, shadowBlocker, dreamsower, defenderBattlefield)).isTrue();
    }
}
