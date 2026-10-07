package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RumblingBaloth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TenaciousDead.class, RumblingBaloth.class})
class TenaciousDeadTest extends BaseCardTest {

    /**
     * Puts Tenacious Dead on the battlefield blocking a lethal attacker, advances to combat
     * damage so it dies, then resolves the queued death trigger up to the may-pay prompt.
     */
    private void killInCombatUntilMayPrompt() {
        killInCombatUntilMayPrompt(new TenaciousDead());
    }

    private void killInCombatUntilMayPrompt(TenaciousDead card) {
        setUpLethalCombat(card);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setUpLethalCombat(TenaciousDead card) {
        Permanent dead = harness.addToBattlefieldAndReturn(player1, card);
        dead.setSummoningSick(false);
        dead.setBlocking(true);
        dead.addBlockingTarget(0);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new RumblingBaloth());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

    }

    @Test
    @DisplayName("Dies, pay {1}{B}, returns to the battlefield tapped")
    void diesPayReturnsTapped() {
        killInCombatUntilMayPrompt();

        harness.assertInGraveyard(player1, "Tenacious Dead");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Tenacious Dead");
        Permanent returned = findPermanent(player1, "Tenacious Dead");
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Dies, decline paying {1}{B}, stays in the graveyard")
    void diesDeclineStaysInGraveyard() {
        killInCombatUntilMayPrompt();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Tenacious Dead");
        harness.assertNotOnBattlefield(player1, "Tenacious Dead");
    }

    @Test
    @DisplayName("Payment cannot be made without black mana")
    void cannotReturnWithoutBlackMana() {
        killInCombatUntilMayPrompt();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Tenacious Dead");
        harness.assertNotOnBattlefield(player1, "Tenacious Dead");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Payment returns only the creature that died, not another copy")
    void returnsOnlyDyingCopy() {
        TenaciousDead other = new TenaciousDead();
        gd.playerGraveyards.get(player1.getId()).add(other);
        killInCombatUntilMayPrompt();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(findPermanents(player1, "Tenacious Dead")).hasSize(1);
        assertThat(findPermanent(player1, "Tenacious Dead").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The dying controller pays and the creature returns under its owner's control")
    void stolenCreatureReturnsToOwner() {
        TenaciousDead card = new TenaciousDead();
        card.setOwnerId(player2.getId());
        killInCombatUntilMayPrompt(card);
        harness.assertInGraveyard(player2, "Tenacious Dead");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player2, "Tenacious Dead");
        harness.assertNotOnBattlefield(player1, "Tenacious Dead");
        assertThat(findPermanent(player2, "Tenacious Dead").isTapped()).isTrue();
    }

    @Test
    @DisplayName("A card that left and reentered the graveyard cannot return from its old death trigger")
    void doesNotReturnAfterLeavingAndReenteringGraveyard() {
        TenaciousDead card = new TenaciousDead();
        setUpLethalCombat(card);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Tenacious Dead");

        // Model a zone round trip while the death trigger is waiting on the stack.
        gd.playerGraveyards.get(player1.getId()).remove(card);
        gd.playerHands.get(player1.getId()).add(card);
        gd.playerHands.get(player1.getId()).remove(card);
        gd.playerGraveyards.get(player1.getId()).add(card);
        gd.markGraveyardEntry(card);

        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Tenacious Dead");
        harness.assertNotOnBattlefield(player1, "Tenacious Dead");
    }
}
