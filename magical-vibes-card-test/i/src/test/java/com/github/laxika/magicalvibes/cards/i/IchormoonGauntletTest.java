package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceThePerfectedMind;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.TeferiTimebender;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IchormoonGauntlet.class, GrizzlyBears.class, Spellbook.class,
        TeferiTimebender.class, JaceThePerfectedMind.class})
class IchormoonGauntletTest extends BaseCardTest {

    @Test
    void grantsProliferateToPlaneswalkersYouControl() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        Permanent teferi = addReadyTeferi(player1, 5);

        int teferiIndex = gd.playerBattlefields.get(player1.getId()).indexOf(teferi);
        harness.activateAbility(player1, teferiIndex, 3, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(teferi.getId()));

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void grantsExtraTurnAbilityToPlaneswalkersYouControl() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        Permanent teferi = addReadyTeferi(player1, 12);

        int teferiIndex = gd.playerBattlefields.get(player1.getId()).indexOf(teferi);
        harness.activateAbility(player1, teferiIndex, 4, null, null);
        harness.passBothPriorities();

        assertThat(gd.extraTurns).contains(player1.getId());
    }

    @Test
    void noncreatureSpellTriggersChosenCounterAddition() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "+1/+1 counters");

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void creatureSpellDoesNotTriggerCounterAddition() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Ichormoon Gauntlet"));
    }

    @Test
    void grantedProliferateAddsEveryCounterKindToSelectedPermanentsAndPlayers() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        Permanent jace = addReadyJace(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IchormoonGauntlet());
        target.setCounterCount(CounterType.OIL, 2);
        target.setCounterCount(CounterType.CHARGE, 3);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        harness.activateAbility(player1, 1, 3, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId(), player2.getId()));

        assertThat(target.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void grantedProliferateMayChooseNothingButStillUsesLoyaltyActivation() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        Permanent jace = addReadyJace(5);

        harness.activateAbility(player1, 1, 3, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedExtraTurnRequiresTwelveLoyalty() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        Permanent jace = addReadyJace(11);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 4, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(11);
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void grantedExtraTurnResolvesAfterPlaneswalkerDiesFromPayingCost() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        addReadyJace(12);

        harness.activateAbility(player1, 1, 4, null, null);
        harness.assertNotOnBattlefield(player1, "Jace, the Perfected Mind");
        harness.assertInGraveyard(player1, "Jace, the Perfected Mind");
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void grantedLoyaltyAbilityCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        addReadyJace(5);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 3, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotGrantAbilitiesToOpponentsPlaneswalkers() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        harness.addToBattlefield(player2, new JaceThePerfectedMind());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 3, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterKindIsChosenFromCountersPresentAtResolution() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IchormoonGauntlet());
        target.setCounterCount(CounterType.OIL, 1);
        prepareGauntletSpell(player1);

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        target.setCounterCount(CounterType.OIL, 0);
        target.setCounterCount(CounterType.CHARGE, 1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "charge counters");

        assertThat(target.getCounterCount(CounterType.OIL)).isZero();
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void counterTriggerCanTargetOpponentsPermanentAndAddsOnlyChosenKind() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IchormoonGauntlet());
        target.setCounterCount(CounterType.OIL, 2);
        target.setCounterCount(CounterType.CHARGE, 3);
        prepareGauntletSpell(player1);

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "oil counters");

        assertThat(target.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void counterTriggerCanTargetPermanentWithoutCountersAndDoesNothing() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IchormoonGauntlet());
        prepareGauntletSpell(player1);

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounters()).isEmpty();
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerCounterAddition() {
        harness.addToBattlefield(player1, new IchormoonGauntlet());
        prepareGauntletSpell(player2);

        harness.castArtifact(player2, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    private void prepareGauntletSpell(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new IchormoonGauntlet()));
        harness.addMana(player, ManaColor.BLUE, 3);
    }

    private Permanent addReadyJace(int loyalty) {
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceThePerfectedMind());
        jace.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return jace;
    }

    private Permanent addReadyTeferi(Player player, int loyalty) {
        Permanent teferi = harness.addToBattlefieldAndReturn(player, new TeferiTimebender());
        teferi.setCounterCount(CounterType.LOYALTY, loyalty);
        teferi.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return teferi;
    }
}
