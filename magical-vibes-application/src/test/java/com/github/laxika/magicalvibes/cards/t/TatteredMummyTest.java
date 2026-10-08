package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.f.FinalReward;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TatteredMummy.class, Colossapede.class, FinalReward.class, TrialOfAmbition.class})
class TatteredMummyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Tattered Mummy puts it on the battlefield")
    void castingPutsOnBattlefield() {
        TatteredMummy mummy = new TatteredMummy();
        harness.castFromHand(player1, mummy, mummy.getManaCost());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tattered Mummy");
    }

    @Test
    @DisplayName("When Tattered Mummy dies, its trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new TatteredMummy());

        setupCombatWhereMummyDies();
        harness.passBothPriorities(); // Combat damage — Tattered Mummy dies

        harness.assertInGraveyard(player1, "Tattered Mummy");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Tattered Mummy");
    }

    @Test
    @DisplayName("Resolving the death trigger makes each opponent lose 2 life, not the controller")
    void deathTriggerCausesOpponentLifeLoss() {
        harness.addToBattlefield(player1, new TatteredMummy());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereMummyDies();
        harness.passBothPriorities(); // Combat damage — Tattered Mummy dies
        harness.passBothPriorities(); // Resolve the death trigger

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("No life is lost while Tattered Mummy stays on the battlefield")
    void noLifeLossWhileAlive() {
        harness.addToBattlefield(player1, new TatteredMummy());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Sacrificing an opponent's Mummy causes life loss for its controller's opponent")
    void sacrificeUsesDyingMummysController() {
        harness.addToBattlefield(player2, new TatteredMummy());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TrialOfAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Tattered Mummy");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling Tattered Mummy does not trigger its death ability")
    void exileDoesNotCauseLifeLoss() {
        Permanent mummy = harness.addToBattlefieldAndReturn(player1, new TatteredMummy());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FinalReward()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, mummy.getId());

        harness.assertNotOnBattlefield(player1, "Tattered Mummy");
        harness.assertNotInGraveyard(player1, "Tattered Mummy");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    /**
     * Sets up combat where Tattered Mummy (player1, 1/2) attacks and is blocked by a 5/5 creature
     * (player2), so the Mummy dies to combat damage.
     */
    private void setupCombatWhereMummyDies() {
        Permanent mummyPerm = findPermanent(player1, "Tattered Mummy");
        mummyPerm.setSummoningSick(false);
        mummyPerm.setAttacking(true);

        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
