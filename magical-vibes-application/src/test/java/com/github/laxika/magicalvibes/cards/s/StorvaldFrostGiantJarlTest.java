package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BurnishedHart;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StorvaldFrostGiantJarl.class, GrizzlyBears.class, Shock.class,
        BurnishedHart.class, SwordsToPlowshares.class})
class StorvaldFrostGiantJarlTest extends BaseCardTest {

    private static final String SEVEN_MODE =
            "Target creature has base power and toughness 7/7 until end of turn";
    private static final String ONE_MODE =
            "Target creature has base power and toughness 1/1 until end of turn";

    @Test
    void wardProtectsStorvaldAndOtherCreaturesYouControl() {
        addReadyStorvald(player1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castShockAt(player2, ownCreature, 1);
        harness.assertInGraveyard(player2, "Shock");

        castShockAt(player2, gd.playerBattlefields.get(player1.getId()).getFirst(), 1);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void wardDoesNotProtectAnOpponentsCreature() {
        addReadyStorvald(player1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castShockAt(player1, opponentCreature, 1);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void entersAndSetsTargetCreatureToSevenSeven() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castStorvald();

        harness.handleListChoice(player1, SEVEN_MODE);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
    }

    @Test
    void attackTriggerSetsTargetCreatureToOneOneUntilEndOfTurn() {
        addReadyStorvald(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        harness.handleListChoice(player1, ONE_MODE);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void bothModesMayTargetTheSameCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castStorvald();

        harness.handleListChoice(player1, SEVEN_MODE);
        harness.handleListChoice(player1, ONE_MODE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void modesResolveInPrintedOrderEvenWhenSelectedInReverseOrder() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        castStorvald();

        harness.handleListChoice(player1, ONE_MODE);
        harness.handleListChoice(player1, SEVEN_MODE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void bothModesHaveIndependentTargetsAndPreserveCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castStorvald();

        harness.handleListChoice(player1, SEVEN_MODE);
        harness.handleListChoice(player1, ONE_MODE);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void remainingTargetStillChangesWhenTheOtherTargetLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        castStorvald();

        harness.handleListChoice(player1, SEVEN_MODE);
        harness.handleListChoice(player1, ONE_MODE);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());

        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, first.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
    }

    @Test
    void payingThreeAllowsSpellToResolveAgainstStorvaldWithoutAnExtraWardCost() {
        Permanent storvald = addReadyStorvald(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        castShockAt(player2, storvald, 1);

        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(storvald.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Storvald, Frost Giant Jarl");
    }

    @Test
    void grantedWardCountersAnOpponentsTriggeredAbility() {
        addReadyStorvald(player2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        castStorvald();

        harness.handleListChoice(player1, ONE_MODE);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyStorvald(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new StorvaldFrostGiantJarl());
    }

    private void castStorvald() {
        harness.setHand(player1, List.of(new StorvaldFrostGiantJarl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void castShockAt(com.github.laxika.magicalvibes.model.Player caster,
                             Permanent target, int mana) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, mana);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
