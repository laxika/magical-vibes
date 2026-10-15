package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KindlyAncestor;
import com.github.laxika.magicalvibes.cards.t.ToxicScorpion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HenrikaDomnathi.class, HenrikaInfernalSeer.class, AirElemental.class, GrizzlyBears.class,
        KindlyAncestor.class, ToxicScorpion.class})
class HenrikaDomnathiTest extends BaseCardTest {

    private static final String SACRIFICE = "Each player sacrifices a creature of their choice";
    private static final String DRAW = "You draw a card and you lose 1 life";
    private static final String TRANSFORM = "Transform Henrika";

    @Test
    void sacrificeModeSacrificesOneCreatureForEachPlayer() {
        Permanent henrika = harness.addToBattlefieldAndReturn(player1, new HenrikaDomnathi());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        beginCombat();
        harness.handleListChoice(player1, SACRIFICE);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, ownCreature.getId())).isNull();
        assertThat(gqs.findPermanentById(gd, opposingCreature.getId())).isNull();
        assertThat(gqs.findPermanentById(gd, henrika.getId())).isNotNull();
    }

    @Test
    void drawModeDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new HenrikaDomnathi());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        beginCombat();
        harness.handleListChoice(player1, DRAW);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void transformModeTransformsHenrika() {
        Permanent henrika = harness.addToBattlefieldAndReturn(player1, new HenrikaDomnathi());

        beginCombat();
        harness.handleListChoice(player1, TRANSFORM);
        harness.passBothPriorities();

        assertThat(henrika.isTransformed()).isTrue();
        assertThat(henrika.getCard()).isInstanceOf(HenrikaInfernalSeer.class);
    }

    @Test
    void chosenModeIsNotOfferedAgain() {
        harness.addToBattlefield(player1, new HenrikaDomnathi());

        beginCombat();
        harness.handleListChoice(player1, DRAW);
        harness.passBothPriorities();

        beginNextTurnCombat();
        assertThatThrownBy(() -> harness.handleListChoice(player1, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void transformedAbilityBoostsOnlyCreaturesWithRelevantKeyword() {
        Permanent henrika = harness.addToBattlefieldAndReturn(player1, new HenrikaDomnathi());
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        beginCombat();
        harness.handleListChoice(player1, TRANSFORM);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int henrikaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(henrika);
        harness.activateAbility(player1, henrikaIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, henrika)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @Test
    void sacrificeModeCanSacrificeHenrikaWhenOpponentHasNoCreatures() {
        Permanent henrika = harness.addToBattlefieldAndReturn(player1, new HenrikaDomnathi());

        beginCombat();
        harness.handleListChoice(player1, SACRIFICE);
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, henrika.getId())).isNull();
        harness.assertInGraveyard(player1, "Henrika Domnathi");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void eachPlayerChoosesTheirOwnSacrificeBeforeEitherCreatureDies() {
        Permanent henrika = harness.addToBattlefieldAndReturn(player1, new HenrikaDomnathi());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstOpponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        beginCombat();
        harness.handleListChoice(player1, SACRIFICE);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId()));

        assertThat(gqs.findPermanentById(gd, ownCreature.getId())).isNotNull();
        assertThat(gqs.findPermanentById(gd, firstOpponent.getId())).isNotNull();
        harness.handleMultiplePermanentsChosen(player2, List.of(secondOpponent.getId()));
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, ownCreature.getId())).isNull();
        assertThat(gqs.findPermanentById(gd, secondOpponent.getId())).isNull();
        assertThat(gqs.findPermanentById(gd, firstOpponent.getId())).isNotNull();
        assertThat(gqs.findPermanentById(gd, henrika.getId())).isNotNull();
    }

    @Test
    void transformedBoostIncludesLifelinkAndDeathtouchButExcludesOpponentsAndLaterEntrants() {
        Permanent henrika = harness.addToBattlefieldAndReturn(player1, new HenrikaDomnathi());
        Permanent lifelinker = harness.addToBattlefieldAndReturn(player1, new KindlyAncestor());
        Permanent scorpion = harness.addToBattlefieldAndReturn(player1, new ToxicScorpion());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new KindlyAncestor());

        beginCombat();
        harness.handleListChoice(player1, TRANSFORM);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(henrika), 0, null, null);
        harness.passBothPriorities();

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new KindlyAncestor());
        assertThat(gqs.getEffectivePower(gd, henrika)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, lifelinker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, scorpion)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, henrika)).isEqualTo(4);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, henrika)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, lifelinker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, scorpion)).isEqualTo(1);
    }

    @Test
    void frontFaceDoesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new HenrikaDomnathi());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private void beginCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    private void beginNextTurnCombat() {
        harness.passUntilWithNoAttackers(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}
