package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CyclonusTheSaboteur.class, CyclonusCybertronianFighter.class, GrizzlyBears.class, Mountain.class})
class CyclonusTheSaboteurTest extends BaseCardTest {

    @Test
    void combatDamageConnivesAndConvertsAtFivePower() {
        Permanent cyclonus = addCreatureReady(player1, new CyclonusTheSaboteur());
        cyclonus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Mountain kept = new Mountain();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(kept)));
        harness.setLibrary(player1, List.of(discarded));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discarded));

        assertThat(cyclonus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(cyclonus.isTransformed()).isTrue();
        assertThat(cyclonus.getCard()).isInstanceOf(CyclonusCybertronianFighter.class);
    }

    @Test
    void convertedCyclonusHasLivingMetal() {
        Permanent cyclonus = castConvertedCyclonus();

        assertThat(cyclonus.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, cyclonus)).isTrue();
    }

    @Test
    void convertedCombatDamageAddsAFullBeginningPhase() {
        Permanent cyclonus = castConvertedCyclonus();
        Permanent tappedMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        tappedMountain.tap();
        Mountain drawn = new Mountain();
        harness.setLibrary(player1, List.of(drawn));

        cyclonus.setSummoningSick(false);
        cyclonus.setAttacking(true);
        cyclonus.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        gd.interaction.clearAwaitingInput();
        harness.resolveCombatDamage();
        harness.assertLife(player2, 15);
        resolveAllTriggers();
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(cyclonus.isTransformed()).isFalse();
        assertThat(tappedMountain.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    private Permanent castConvertedCyclonus() {
        harness.setHand(player1, List.of(new CyclonusTheSaboteur()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Cyclonus, Cybertronian Fighter");
    }
}
