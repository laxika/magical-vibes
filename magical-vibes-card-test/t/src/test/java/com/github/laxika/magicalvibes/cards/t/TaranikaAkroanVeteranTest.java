package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TaranikaAkroanVeteran.class, GrizzlyBears.class})
class TaranikaAkroanVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping an attacking creature leaves it attacking, and Taranika attacks without tapping")
    void untappingAnAttackerDoesNotRemoveItFromCombat() {
        Permanent taranika = addCreatureReady(player1, new TaranikaAkroanVeteran());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1));
            assertThat(taranika.isTapped()).isFalse();
            assertThat(bears.isTapped()).isTrue();
            harness.handlePermanentChosen(player1, bears.getId());
            harness.passBothPriorities();
        });

        assertThat(bears.isTapped()).isFalse();
        assertThat(bears.isAttacking()).isTrue();
        assertThat(taranika.isAttacking()).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The 4/4 base stats still include existing +1/+1 counters")
    void baseStatsPreserveCounters() {
        addCreatureReady(player1, new TaranikaAkroanVeteran());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A stun counter prevents untapping but does not prevent the other effects")
    void blockedUntapStillSetsStatsAndGrantsIndestructible() {
        addCreatureReady(player1, new TaranikaAkroanVeteran());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();
        bears.setCounterCount(CounterType.STUN, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The attack trigger resolves even if Taranika leaves the battlefield")
    void sourceLeavingDoesNotStopTrigger() {
        Permanent taranika = addCreatureReady(player1, new TaranikaAkroanVeteran());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(taranika);
        gd.playerGraveyards.get(player1.getId()).add(taranika.getCard());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A target no longer controlled by the trigger's controller receives none of its effects")
    void targetChangingControllerMakesTriggerIllegal() {
        addCreatureReady(player1, new TaranikaAkroanVeteran());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Attacking untaps another creature and makes it a 4/4 indestructible creature")
    void attackTriggerUntapsAndStrengthensAnotherCreature() {
        addCreatureReady(player1, new TaranikaAkroanVeteran());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The temporary power, toughness, and indestructible effects wear off at end of turn")
    void temporaryEffectsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new TaranikaAkroanVeteran());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger cannot target Taranika or an opponent's creature")
    void attackTriggerRequiresAnotherCreatureYouControl() {
        Permanent taranika = addCreatureReady(player1, new TaranikaAkroanVeteran());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent legalTarget = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, taranika.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, legalTarget)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, legalTarget, Keyword.INDESTRUCTIBLE)).isTrue();
    }

}
