package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadyTraveler.class, StalkingPredator.class, DawnhartRejuvenator.class})
class ShadyTravelerTest extends BaseCardTest {

    @Test
    void dayAndNightTransformTheFaces() {
        gd.dayNight = DayNight.DAY;
        Permanent traveler = addCreatureReady(player1, new ShadyTraveler());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);
        assertThat(traveler.isTransformed()).isTrue();
        assertThat(traveler.getCard()).isInstanceOf(StalkingPredator.class);

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        harness.performUntapStep(player2);
        assertThat(traveler.isTransformed()).isFalse();
        assertThat(traveler.getCard()).isInstanceOf(ShadyTraveler.class);
    }

    @Test
    void menaceRequiresTwoBlockers() {
        Permanent traveler = addCreatureReady(player1, new ShadyTraveler());
        traveler.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DawnhartRejuvenator());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(traveler)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void menaceAllowsTwoBlockers() {
        Permanent traveler = addCreatureReady(player1, new ShadyTraveler());
        traveler.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new DawnhartRejuvenator());
        Permanent secondBlocker = addCreatureReady(player2, new DawnhartRejuvenator());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(traveler)),
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(traveler))));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    void enteringBeforeDayOrNightEstablishesDay() {
        Permanent traveler = harness.enterBattlefieldAndReturn(player1, new ShadyTraveler());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(traveler.isTransformed()).isFalse();
    }

    @Test
    void enteringAtNightEntersAsStalkingPredator() {
        gd.dayNight = DayNight.NIGHT;

        Permanent traveler = harness.enterBattlefieldAndReturn(player1, new ShadyTraveler());

        assertThat(traveler.isTransformed()).isTrue();
        assertThat(traveler.getCard()).isInstanceOf(StalkingPredator.class);
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
    }

    @Test
    void opponentsSpellsDoNotPreventNightWhenActivePlayerCastNone() {
        gd.dayNight = DayNight.DAY;
        Permanent traveler = harness.enterBattlefieldAndReturn(player1, new ShadyTraveler());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(traveler.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void oneSpellByActivePlayerKeepsDay() {
        gd.dayNight = DayNight.DAY;
        Permanent traveler = harness.enterBattlefieldAndReturn(player1, new ShadyTraveler());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(traveler.isTransformed()).isFalse();
    }

    @Test
    void nonactivePlayersTwoSpellsNeitherMakeDayNorCreateAnUpkeepAbility() {
        gd.dayNight = DayNight.NIGHT;
        Permanent traveler = harness.enterBattlefieldAndReturn(player1, new ShadyTraveler());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(traveler.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Stalking Predator's upkeep ability triggers.")).isFalse();
    }

    @Test
    void nightFaceMenaceRequiresTwoBlockers() {
        gd.dayNight = DayNight.NIGHT;
        Permanent traveler = harness.enterBattlefieldAndReturn(player1, new ShadyTraveler());
        traveler.setSummoningSick(false);
        traveler.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DawnhartRejuvenator());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(traveler)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nightFaceMenaceAllowsTwoBlockers() {
        gd.dayNight = DayNight.NIGHT;
        Permanent traveler = harness.enterBattlefieldAndReturn(player1, new ShadyTraveler());
        traveler.setSummoningSick(false);
        traveler.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new DawnhartRejuvenator());
        Permanent secondBlocker = addCreatureReady(player2, new DawnhartRejuvenator());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(traveler)),
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(traveler))));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
