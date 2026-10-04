package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObuunMulDayaAncestor.class, Forest.class, GrizzlyBears.class})
class ObuunMulDayaAncestorTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat animates up to one land using Obuun's power")
    void beginningOfCombatAnimatesLandUsingObuunsPower() {
        Permanent obuun = addObuun();
        obuun.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToBeginningOfCombat(player1);

        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest)).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Beginning of combat may animate no land")
    void beginningOfCombatMayAnimateNoLand() {
        addObuun();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToBeginningOfCombat(player1);

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("The land animation ends at the end of the turn")
    void landAnimationEndsAtEndOfTurn() {
        addObuun();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        declareAttackers(List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on any target creature")
    void landfallPutsCounterOnTargetCreature() {
        addObuun();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's landfall does not trigger Obuun")
    void opponentsLandfallDoesNotTrigger() {
        addObuun();
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addObuun() {
        return harness.addToBattlefieldAndReturn(player1, new ObuunMulDayaAncestor());
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
