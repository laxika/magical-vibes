package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InsideSource;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarleadersCall.class, GrizzlyBears.class, InsideSource.class, Opalescence.class})
class WarleadersCallTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +1/+1")
    void buffsOwnCreatures() {
        harness.addToBattlefield(player1, new WarleadersCall());
        var ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature entering under your control deals 1 damage to each opponent")
    void damagesEachOpponentWhenOwnCreatureEnters() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WarleadersCall());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A creature entering under an opponent's control does not trigger the damage ability")
    void doesNotTriggerForOpponentCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WarleadersCall());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Both the entering creature and its creature token trigger the damage ability")
    void triggersForCreatureTokensAndBoostsThem() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WarleadersCall());

        harness.castFromHand(player1, new InsideSource(), "{2}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        var detective = findPermanent(player1, "Detective");
        assertThat(gqs.getEffectivePower(gd, detective)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, detective)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple copies each boost creatures and trigger independently")
    void multipleCopiesStackBonusesAndDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WarleadersCall());
        harness.addToBattlefield(player1, new WarleadersCall());

        harness.castFromHand(player1, new InsideSource(), "{2}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        var source = findPermanent(player1, "Inside Source");
        var detective = findPermanent(player1, "Detective");
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, detective)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, detective)).isEqualTo(4);
    }

    @Test
    @DisplayName("An already-triggered ability resolves after the enchantment leaves")
    void pendingTriggerSurvivesSourceRemovalAndBonusEnds() {
        harness.setLife(player2, 20);
        var call = harness.addToBattlefieldAndReturn(player1, new WarleadersCall());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        var bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        harness.assertLife(player2, 20);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, call));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Warleader's Call");
        harness.assertLife(player2, 19);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Warleader's Call gets its own bonus when Opalescence makes it a creature")
    void animatedCallBoostsItself() {
        harness.addToBattlefield(player1, new Opalescence());
        var call = harness.addToBattlefieldAndReturn(player1, new WarleadersCall());

        assertThat(gqs.getEffectivePower(gd, call)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, call)).isEqualTo(4);
    }

    @Test
    @DisplayName("Warleader's Call triggers for itself when it enters as a creature")
    void animatedCallTriggersForItsOwnEntry() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Opalescence());

        harness.castFromHand(player1, new WarleadersCall(), "{1}{R}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Warleader's Call");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
