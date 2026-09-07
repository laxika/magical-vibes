package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HenrikaDomnathi.class, HenrikaInfernalSeer.class, AirElemental.class, GrizzlyBears.class})
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
        harness.handlePermanentChosen(player1, ownCreature.getId());
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

    private void beginCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    private void beginNextTurnCombat() {
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}
