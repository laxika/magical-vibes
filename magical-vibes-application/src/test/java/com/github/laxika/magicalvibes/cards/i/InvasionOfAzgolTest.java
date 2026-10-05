package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AshenReaper;
import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HiddenPredators;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvasionOfAzgol.class, AshenReaper.class, GrizzlyBears.class, Disenchant.class,
        HiddenPredators.class, ChandraHopesBeacon.class})
class InvasionOfAzgolTest extends BaseCardTest {

    @Test
    void entersAndTargetPlayerSacrificesCreatureAndLosesLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InvasionOfAzgol()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        gs.playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void transformedAshenReaperDoesNotGetCounterWithoutGraveyardPermanent() {
        Permanent battle = addBattleWithNoDefenseCounters();

        defeatBattle(battle);
        Permanent reaper = findPermanent(player1, "Ashen Reaper");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(reaper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void transformedAshenReaperGetsCounterWhenNoncreaturePermanentEntersGraveyard() {
        Permanent battle = addBattleWithNoDefenseCounters();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new HiddenPredators());

        defeatBattle(battle);
        Permanent reaper = findPermanent(player1, "Ashen Reaper");

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, enchantment.getId());
        harness.assertNotOnBattlefield(player2, "Hidden Predators");
        harness.assertInGraveyard(player2, "Hidden Predators");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(reaper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void targetWithoutCreatureOrPlaneswalkerStillLosesLife() {
        harness.addToBattlefield(player2, new InvasionOfAzgol());
        int lifeBefore = gd.getLife(player2.getId());

        castInvasionTargeting(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Invasion of Azgol");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void canTargetControllerToSacrificeOwnCreature() {
        defeatBattle(addBattleWithNoDefenseCounters());
        int lifeBefore = gd.getLife(player1.getId());

        castInvasionTargeting(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ashen Reaper");
        harness.assertInGraveyard(player1, "Invasion of Azgol");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void targetPlayerCanChoosePlaneswalkerInsteadOfCreature() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfAzgol());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        defeatBattle(battle);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        int lifeBefore = gd.getLife(player2.getId());

        castInvasionTargeting(player2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(planeswalker.getId()));

        harness.assertOnBattlefield(player2, "Ashen Reaper");
        harness.assertNotOnBattlefield(player2, "Chandra, Hope's Beacon");
        harness.assertInGraveyard(player2, "Chandra, Hope's Beacon");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void controllerCanDeclineCastingDefeatedBattleTransformed() {
        Permanent battle = addBattleWithNoDefenseCounters();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();

        assertThat(gd.hasPendingInteraction(PendingInteraction.MayAbilityChoice.class)).isTrue();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(battle.getCard().getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Invasion of Azgol");
        harness.assertNotOnBattlefield(player1, "Ashen Reaper");
    }

    @Test
    void reaperGetsCounterWhenFirstPermanentDiesInResponseToEndStepTrigger() {
        Permanent battle = addBattleWithNoDefenseCounters();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new HiddenPredators());
        defeatBattle(battle);
        Permanent reaper = findPermanent(player1, "Ashen Reaper");
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).anySatisfy(entry ->
                assertThat(entry.getSourcePermanentId()).isEqualTo(reaper.getId()));
        harness.castAndResolveInstant(player1, 0, enchantment.getId());
        harness.passBothPriorities();

        assertThat(reaper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void reaperDoesNotTriggerDuringOpponentsEndStep() {
        Permanent battle = addBattleWithNoDefenseCounters();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new HiddenPredators());
        defeatBattle(battle);
        Permanent reaper = findPermanent(player1, "Ashen Reaper");
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, enchantment.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(reaper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void reaperCountsPermanentThatDiedBeforeItEntered() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new HiddenPredators());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, enchantment.getId());
        Permanent battle = addBattleWithNoDefenseCounters();
        defeatBattle(battle);
        Permanent reaper = findPermanent(player1, "Ashen Reaper");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(reaper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castInvasionTargeting(Player targetPlayer) {
        harness.setHand(player1, List.of(new InvasionOfAzgol()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        gs.playCard(gd, player1, 0, 0, targetPlayer.getId(), null);
        harness.passBothPriorities();
    }

    private Permanent addBattleWithNoDefenseCounters() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfAzgol());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        return battle;
    }

    private void defeatBattle(Permanent battle) {
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
