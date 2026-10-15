package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PillarOfFlame;
import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
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

@CardUsed({SoulcageFiend.class, Vorstclaw.class, PillarOfFlame.class})
class SoulcageFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Soulcage Fiend puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.castFromHand(player1, new SoulcageFiend(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Soulcage Fiend");
    }

    @Test
    @DisplayName("When Soulcage Fiend dies, its trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new SoulcageFiend());

        setupCombatWhereFiendDies();
        harness.passBothPriorities(); // Combat damage — Soulcage Fiend dies

        harness.assertInGraveyard(player1, "Soulcage Fiend");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Soulcage Fiend");
    }

    @Test
    @DisplayName("Resolving the death trigger makes each player, including the controller, lose 3 life")
    void deathTriggerCausesEachPlayerLifeLoss() {
        harness.addToBattlefield(player1, new SoulcageFiend());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereFiendDies();
        harness.passBothPriorities(); // Combat damage — Soulcage Fiend dies
        harness.passBothPriorities(); // Resolve the death trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("No life is lost while Soulcage Fiend stays on the battlefield")
    void noLifeLossWhileAlive() {
        harness.addToBattlefield(player1, new SoulcageFiend());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Two Fiends dying in combat each make both players lose 3 life")
    void simultaneousDeathsEachTrigger() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SoulcageFiend());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SoulcageFiend());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soulcage Fiend");
        harness.assertInGraveyard(player2, "Soulcage Fiend");
        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.passBothPriorities();
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 14);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exile replacing death does not trigger life loss")
    void exileInsteadOfDeathDoesNotTrigger() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player2, new SoulcageFiend());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, fiend.getId());

        harness.assertNotOnBattlefield(player2, "Soulcage Fiend");
        harness.assertNotInGraveyard(player2, "Soulcage Fiend");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Soulcage Fiend"));
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    /**
     * Sets up combat where Soulcage Fiend (player1, 3/2) attacks and is blocked by Vorstclaw (7/7)
     * (player2), so the Fiend dies to combat damage.
     */
    private void setupCombatWhereFiendDies() {
        Permanent fiendPerm = findPermanent(player1, "Soulcage Fiend");
        fiendPerm.setSummoningSick(false);
        fiendPerm.setAttacking(true);

        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
