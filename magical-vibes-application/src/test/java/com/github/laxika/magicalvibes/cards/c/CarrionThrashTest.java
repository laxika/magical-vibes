package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CarrionThrashTest extends BaseCardTest {

    /**
     * Puts Carrion Thrash on the battlefield blocking a lethal 5/5 attacker, advances to combat
     * damage so it dies, then resolves the queued death trigger up to the may-pay prompt.
     */
    private void killInCombatUntilMayPrompt() {
        CarrionThrash thrash = new CarrionThrash();
        Permanent thrashPerm = new Permanent(thrash);
        thrashPerm.setSummoningSick(false);
        thrashPerm.setBlocking(true);
        thrashPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player1.getId()).add(thrashPerm);

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(5);
        bears.setToughness(5);
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // advance to combat damage → Carrion Thrash dies
        harness.passBothPriorities(); // resolve death trigger → may-pay prompt
    }

    @Test
    @DisplayName("Dies, pay {2}, returns another target creature card from graveyard to hand")
    void diesPayReturnsAnotherCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        killInCombatUntilMayPrompt();

        // Carrion Thrash is now in the graveyard alongside the pre-seeded Grizzly Bears
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Carrion Thrash"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        // Accept and pay {2} → graveyard creature choice
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // "Another" — Carrion Thrash cannot return itself
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Carrion Thrash"));
    }

    @Test
    @DisplayName("Dies, decline paying {2}, nothing is returned")
    void diesDeclineReturnsNothing() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        killInCombatUntilMayPrompt();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Only non-creature cards (besides itself) in graveyard — no creature to return")
    void diesWithNoOtherCreatureCard() {
        // Wrath of God is a sorcery; the only creature card would be Carrion Thrash itself, which
        // "another" excludes.
        harness.setGraveyard(player1, List.of(new WrathOfGod()));

        killInCombatUntilMayPrompt();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        // No graveyard choice is offered and Carrion Thrash stays put
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Carrion Thrash"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Carrion Thrash"));
    }
}
