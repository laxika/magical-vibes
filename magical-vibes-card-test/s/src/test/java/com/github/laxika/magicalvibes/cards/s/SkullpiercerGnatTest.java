package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkullpiercerGnat.class, Forest.class, GrizzlyBears.class})
class SkullpiercerGnatTest extends BaseCardTest {

    @Test
    void toxicAppliesWithDamageBeforeTheHandModificationTriggerResolves() {
        Permanent attacker = addCreatureReady(player1, new SkullpiercerGnat());
        attacker.setAttacking(true);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();

        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void multipleHitsGrantSeparatePoisonAbilitiesToTheSameCard() {
        addCreatureReady(player1, new SkullpiercerGnat());
        addCreatureReady(player1, new SkullpiercerGnat());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void landOnlyHandIsUnaffectedAndPlayingLandDoesNotGivePoison() {
        addCreatureReady(player1, new SkullpiercerGnat());
        harness.setHand(player2, List.of(new Forest()));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void emptyHandDoesNotPreventCombatDamageOrToxic() {
        addCreatureReady(player1, new SkullpiercerGnat());
        harness.setHand(player2, List.of());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void combatDamageMarksRandomNonlandHandCardToGivePoisonOnCast() {
        addCreatureReady(player1, new SkullpiercerGnat());
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 1);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }
}
