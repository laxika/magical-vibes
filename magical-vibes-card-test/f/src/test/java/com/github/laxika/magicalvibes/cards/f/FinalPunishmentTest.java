package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Carbonize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({FinalPunishment.class, Carbonize.class})
class FinalPunishmentTest extends BaseCardTest {

    @Test
    @DisplayName("Target loses life equal to the damage dealt to them this turn")
    void losesLifeEqualToDamageThisTurn() {
        carbonizePlayer(player2.getId());
        harness.assertLife(player2, 17);

        castFinalPunishment(player2.getId());

        // 3 damage dealt this turn -> loses 3 more life (17 -> 14)
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Accumulates damage from multiple sources this turn")
    void accumulatesDamageFromMultipleSources() {
        carbonizePlayer(player2.getId());
        carbonizePlayer(player2.getId());
        harness.assertLife(player2, 14);

        castFinalPunishment(player2.getId());

        // 6 damage dealt this turn -> loses 6 more life (14 -> 8)
        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("Uses damage dealt rather than the target's current life total")
    void usesDamageAmountInsteadOfCurrentLifeTotal() {
        carbonizePlayer(player2.getId());
        harness.setLife(player2, 30);

        castFinalPunishment(player2.getId());

        harness.assertLife(player2, 27);
    }

    @Test
    @DisplayName("Does not count Final Punishment's own life loss as damage")
    void doesNotCountItsOwnLifeLossAsDamage() {
        carbonizePlayer(player2.getId());
        castFinalPunishment(player2.getId());
        harness.assertLife(player2, 14);

        castFinalPunishment(player2.getId());

        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("Target that took no damage loses no life")
    void noDamageMeansNoLifeLoss() {
        carbonizePlayer(player2.getId());

        // Final Punishment targets player1, who took no damage this turn
        castFinalPunishment(player1.getId());

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not count damage that was prevented")
    void ignoresPreventedDamage() {
        gd.playerDamagePreventionShields.put(player2.getId(), 3);

        carbonizePlayer(player2.getId());
        harness.assertLife(player2, 20);

        castFinalPunishment(player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not count damage dealt during a previous turn")
    void ignoresDamageFromPreviousTurn() {
        carbonizePlayer(player2.getId());
        harness.assertLife(player2, 17);

        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        castFinalPunishment(player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Can target its controller after they have taken damage")
    void controllerCanLoseLifeFromTheirOwnDamageTotal() {
        carbonizePlayer(player1.getId());

        castFinalPunishment(player1.getId());

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Counts only the unprevented portion of damage")
    void countsPartiallyPreventedDamage() {
        gd.playerDamagePreventionShields.put(player2.getId(), 2);
        carbonizePlayer(player2.getId());
        harness.assertLife(player2, 19);

        castFinalPunishment(player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Includes damage dealt in response before Final Punishment resolves")
    void evaluatesDamageTotalAtResolution() {
        harness.setHand(player1, List.of(new FinalPunishment(), new Carbonize()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 17);
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    private void carbonizePlayer(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Carbonize()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }

    private void castFinalPunishment(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new FinalPunishment()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }
}
