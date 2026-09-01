package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({MayhemPatrol.class, GrizzlyBears.class})
class MayhemPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts any target creature by +1/+0 until end of turn")
    void attackTriggerBoostsTargetCreature() {
        addReadyCreature(player1, new MayhemPatrol());
        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new MayhemPatrol()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent patrol = findPermanent(player1, "Mayhem Patrol");
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mayhem Patrol");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

}
