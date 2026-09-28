package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaveOfRats.class, GrizzlyBears.class})
class WaveOfRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Normal casting does not grant blitz haste or delayed sacrifice")
    void normalCastDoesNotUseBlitz() {
        harness.setHand(player1, List.of(new WaveOfRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent rats = findPermanent(player1, "Wave of Rats");
        assertThat(gqs.hasKeyword(gd, rats, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rats);
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new WaveOfRats()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rats = findPermanent(player1, "Wave of Rats");
        assertThat(gqs.hasKeyword(gd, rats, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Wave of Rats");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns to the battlefield when it dies after dealing combat damage to a player")
    void returnsAfterDealingCombatDamageToPlayerThisTurn() {
        Permanent rats = addCreatureReady(player1, new WaveOfRats());
        rats.setAttacking(true);

        resolveCombat();
        rats.setMarkedDamage(rats.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wave of Rats");
        harness.assertNotInGraveyard(player1, "Wave of Rats");
    }

    @Test
    @DisplayName("Does not return when it dies without dealing combat damage to a player")
    void doesNotReturnWithoutCombatDamageToPlayer() {
        Permanent rats = addCreatureReady(player1, new WaveOfRats());
        rats.setMarkedDamage(rats.getEffectiveToughness());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wave of Rats");
        harness.assertInGraveyard(player1, "Wave of Rats");
    }
}
