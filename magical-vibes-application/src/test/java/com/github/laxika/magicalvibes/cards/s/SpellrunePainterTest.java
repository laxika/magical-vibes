package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellrunePainter.class, SpellruneHowler.class, Shock.class, Ponder.class, GrizzlyBears.class, Duress.class})
class SpellrunePainterTest extends BaseCardTest {

    @Test
    void painterGetsPlusOnePlusOneForInstantOrSorcery() {
        Permanent painter = addPainter();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(painter.getPowerModifier()).isEqualTo(1);
        assertThat(painter.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void painterDoesNotTriggerForCreatureSpell() {
        Permanent painter = addPainter();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(painter.getPowerModifier()).isZero();
        assertThat(painter.getToughnessModifier()).isZero();
    }

    @Test
    void howlerGetsPlusTwoPlusTwoAfterBecomingNight() {
        gd.dayNight = DayNight.DAY;
        Permanent painter = addPainter();

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(painter.isTransformed()).isTrue();
        assertThat(painter.getCard()).isInstanceOf(SpellruneHowler.class);

        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(painter.getPowerModifier()).isEqualTo(2);
        assertThat(painter.getToughnessModifier()).isEqualTo(2);
    }

    private Permanent addPainter() {
        Permanent painter = harness.addToBattlefieldAndReturn(player1, new SpellrunePainter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return painter;
    }

    @Test
    void painterTriggersForSorceryBeforeItResolves() {
        Permanent painter = addPainter();
        harness.setHand(player1, List.of(new Duress()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(painter.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(painter.getPowerModifier()).isEqualTo(1);
        assertThat(painter.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    void opponentsInstantDoesNotBoostEitherFace() {
        Permanent painter = addPainter();
        Permanent howler = harness.addToBattlefieldAndReturn(player1, new SpellruneHowler());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(painter.getPowerModifier()).isZero();
        assertThat(painter.getToughnessModifier()).isZero();
        assertThat(howler.getPowerModifier()).isZero();
        assertThat(howler.getToughnessModifier()).isZero();
    }

    @Test
    void repeatedInstantsBoostEachFaceIndependentlyAndExpireAtCleanup() {
        Permanent painter = addPainter();
        Permanent howler = harness.addToBattlefieldAndReturn(player1, new SpellruneHowler());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(painter.getPowerModifier()).isEqualTo(2);
        assertThat(painter.getToughnessModifier()).isEqualTo(2);
        assertThat(howler.getPowerModifier()).isEqualTo(4);
        assertThat(howler.getToughnessModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(painter.getPowerModifier()).isZero();
        assertThat(painter.getToughnessModifier()).isZero();
        assertThat(howler.getPowerModifier()).isZero();
        assertThat(howler.getToughnessModifier()).isZero();
    }

    @Test
    void howlerDoesNotTriggerForCreatureSpell() {
        addPainter();
        Permanent howler = harness.addToBattlefieldAndReturn(player1, new SpellruneHowler());
        harness.setHand(player1, List.of(new SpellrunePainter()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(howler.getPowerModifier()).isZero();
        assertThat(howler.getToughnessModifier()).isZero();
    }

    @Test
    void enteringPainterEstablishesDay() {
        Permanent painter = harness.enterBattlefieldAndReturn(player1, new SpellrunePainter());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(painter.isTransformed()).isFalse();
    }

    @Test
    void enteringPainterAtNightUsesHowlerAbility() {
        gd.dayNight = DayNight.NIGHT;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent painter = harness.enterBattlefieldAndReturn(player1, new SpellrunePainter());
        assertThat(painter.isTransformed()).isTrue();
        harness.setHand(player1, List.of(new Duress()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(painter.getPowerModifier()).isEqualTo(2);
        assertThat(painter.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void twoSpellsOnPreviousActivePlayersTurnRestorePainterAbility() {
        gd.dayNight = DayNight.NIGHT;
        Permanent painter = harness.enterBattlefieldAndReturn(player1, new SpellrunePainter());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        harness.performUntapStep(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(painter.isTransformed()).isFalse();
        harness.setHand(player1, List.of(new Duress()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(painter.getPowerModifier()).isEqualTo(1);
        assertThat(painter.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void oneSpellDuringDayKeepsPainterFace() {
        gd.dayNight = DayNight.DAY;
        Permanent painter = addPainter();
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(painter.isTransformed()).isFalse();
    }

    @Test
    void spellsByNonactivePlayerDoNotRestoreDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent painter = harness.enterBattlefieldAndReturn(player1, new SpellrunePainter());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(painter.isTransformed()).isTrue();
    }
}
