package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BanebladeScoundrel.class, BaneclawMarauder.class, CandlegroveWitch.class})
class BanebladeScoundrelTest extends BaseCardTest {

    @Test
    @DisplayName("When it becomes blocked, each blocker gets -1/-1")
    void weakensEachBlocker() {
        Permanent scoundrel = addCreatureReady(player1, new BanebladeScoundrel());
        scoundrel.setAttacking(true);
        Permanent blocker1 = addCreatureReady(player2, new CandlegroveWitch());
        Permanent blocker2 = addCreatureReady(player2, new CandlegroveWitch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blocker1)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker1)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, blocker2)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Transforms into Baneclaw Marauder when no spells were cast last turn")
    void transformsToBack() {
        Permanent scoundrel = addCreatureReady(player1, new BanebladeScoundrel());
        gd.dayNight = DayNight.DAY;
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(scoundrel.isTransformed()).isTrue();
        assertThat(scoundrel.getCard()).isInstanceOf(BaneclawMarauder.class);
    }

    @Test
    @DisplayName("Baneclaw Marauder transforms back when the previous active player cast two spells")
    void transformsBack() {
        Permanent scoundrel = addCreatureReady(player1, new BanebladeScoundrel());
        transformToBack(scoundrel);
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        harness.forceActivePlayer(player1);
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(scoundrel.isTransformed()).isFalse();
        assertThat(scoundrel.getCard()).isInstanceOf(BanebladeScoundrel.class);
    }

    @Test
    @DisplayName("Baneclaw Marauder makes the controller of a dying blocker lose 1 life")
    void blockerDeathCausesLifeLoss() {
        Permanent marauder = addBackFace(player1);
        marauder.setAttacking(true);
        Permanent otherAttacker = addCreatureReady(player1, new CandlegroveWitch());
        otherAttacker.setAttacking(true);
        addCreatureReady(player2, new CandlegroveWitch());
        addCreatureReady(player2, new CandlegroveWitch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The front face weakens multiple blockers with one ability on the stack")
    void frontFaceTriggersOnceForMultipleBlockers() {
        assertSingleWeakeningTrigger(false);
    }

    @Test
    @DisplayName("The back face weakens multiple blockers with one ability on the stack")
    void backFaceTriggersOnceForMultipleBlockers() {
        assertSingleWeakeningTrigger(true);
    }

    @Test
    @DisplayName("Both blockers get -1/-1 and each dying blocker causes life loss at night")
    void backFaceWeakensAndDrainsForEachBlocker() {
        Permanent marauder = addBackFace(player1);
        marauder.setAttacking(true);
        Permanent blocker1 = addCreatureReady(player2, new CandlegroveWitch());
        Permanent blocker2 = addCreatureReady(player2, new CandlegroveWitch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blocker1)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker1)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, blocker2)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker2)).isEqualTo(1);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker1, blocker2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The front face does not drain life when a blocker dies")
    void frontFaceDoesNotDrainForDyingBlocker() {
        Permanent scoundrel = addCreatureReady(player1, new BanebladeScoundrel());
        scoundrel.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CandlegroveWitch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Daybound checks the previous turn's active player, not the incoming player")
    void staysDayWhenPreviousActivePlayerCastOneSpell() {
        Permanent scoundrel = addCreatureReady(player1, new BanebladeScoundrel());
        gd.dayNight = DayNight.DAY;
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(scoundrel.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("An opponent's two spells do not make it day after the active player cast one")
    void staysNightWhenOnlyNonactivePlayerCastTwoSpells() {
        Permanent marauder = addBackFace(player1);
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player1);
        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(marauder.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Skipping untap does not create a daybound upkeep trigger")
    void skippedUntapDoesNotTriggerTransformationAtUpkeep() {
        Permanent scoundrel = addCreatureReady(player1, new BanebladeScoundrel());
        gd.dayNight = DayNight.DAY;
        gd.skipNextUntapStepCount.put(player2.getId(), 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(scoundrel.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting the card when neither day nor night starts day")
    void enteringStartsDay() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BanebladeScoundrel(), "{3}{B}");
        resolveAllTriggers();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(findPermanent(player1, "Baneblade Scoundrel").isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Casting the card at night makes it enter with its back face up")
    void entersTransformedAtNight() {
        gd.dayNight = DayNight.NIGHT;
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BanebladeScoundrel(), "{3}{B}");
        resolveAllTriggers();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(findPermanent(player1, "Baneclaw Marauder").isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The weakening of a surviving blocker expires at end of turn")
    void weakeningExpiresAtEndOfTurn() {
        Permanent scoundrel = addCreatureReady(player1, new BanebladeScoundrel());
        scoundrel.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CandlegroveWitch());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    private void assertSingleWeakeningTrigger(boolean backFace) {
        Permanent attacker = backFace ? addBackFace(player1)
                : addCreatureReady(player1, new BanebladeScoundrel());
        attacker.setAttacking(true);
        addCreatureReady(player2, new CandlegroveWitch());
        addCreatureReady(player2, new CandlegroveWitch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addBackFace(Player player) {
        Permanent marauder = addCreatureReady(player, new BanebladeScoundrel());
        transformToBack(marauder);
        return marauder;
    }

    private void transformToBack(Permanent permanent) {
        gd.dayNight = DayNight.DAY;
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(permanent.isTransformed()).isTrue();
    }
}
