package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HiddenDragonslayer.class, HillGiant.class, GrizzlyBears.class})
class HiddenDragonslayerTest extends BaseCardTest {

    @Test
    void megamorphPutsCounterOnItAndDestroysTargetOpponentCreatureWithPowerAtLeastFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setPowerModifier(1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent dragonslayer = castFaceDown();

        turnFaceUp(dragonslayer);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(dragonslayer.isFaceDown()).isFalse();
        assertThat(dragonslayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void megamorphHasNoTargetWhenOnlyOwnOrLowPowerCreaturesExist() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        ownCreature.setPowerModifier(1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent dragonslayer = castFaceDown();

        turnFaceUp(dragonslayer);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(dragonslayer.isFaceDown()).isFalse();
        assertThat(dragonslayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void turningFaceUpWithoutPayingMegamorphStillDestroysButDoesNotAddCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setPowerModifier(1);
        Permanent dragonslayer = castFaceDown();

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, dragonslayer);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(dragonslayer.isFaceDown()).isFalse();
        assertThat(dragonslayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterIsPlacedBeforeTheDestructionTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setPowerModifier(1);
        Permanent dragonslayer = castFaceDown();

        turnFaceUp(dragonslayer);

        assertThat(dragonslayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(dragonslayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void targetSurvivesIfItsPowerDropsBelowFourBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setPowerModifier(1);
        Permanent dragonslayer = castFaceDown();
        turnFaceUp(dragonslayer);
        harness.handlePermanentChosen(player1, target.getId());

        target.setPowerModifier(0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");
        assertThat(dragonslayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void targetSurvivesIfItsControllerBecomesTheTriggerController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setPowerModifier(1);
        Permanent dragonslayer = castFaceDown();
        turnFaceUp(dragonslayer);
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");
    }

    @Test
    void castingFaceUpDoesNotTriggerDestructionOrAddCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setPowerModifier(1);
        harness.setHand(player1, List.of(new HiddenDragonslayer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Hidden Dragonslayer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void faceUpCombatDamageGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent dragonslayer = addCreatureReady(player1, new HiddenDragonslayer());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(dragonslayer)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void faceDownCombatDamageDoesNotGainLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent dragonslayer = castFaceDown();
        dragonslayer.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(dragonslayer)));
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new HiddenDragonslayer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Hidden Dragonslayer");
    }

    private void turnFaceUp(Permanent dragonslayer) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dragonslayer));
    }
}
