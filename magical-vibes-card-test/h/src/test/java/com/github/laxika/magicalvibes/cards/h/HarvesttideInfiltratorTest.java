package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.u.UnrulyMob;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarvesttideInfiltrator.class, HarvesttideAssailant.class, UnrulyMob.class})
class HarvesttideInfiltratorTest extends BaseCardTest {

    @Test
    void becomesDayWhenItEntersWithoutADesignation() {
        castInfiltrator();

        Permanent infiltrator = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(infiltrator.isTransformed()).isFalse();
        assertThat(infiltrator.getCard()).isInstanceOf(HarvesttideInfiltrator.class);
    }

    @Test
    void transformsToAssailantWhenItBecomesNight() {
        gd.dayNight = DayNight.DAY;
        Permanent infiltrator = addCreatureReady(player1, new HarvesttideInfiltrator());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(infiltrator.isTransformed()).isTrue();
        assertThat(infiltrator.getCard()).isInstanceOf(HarvesttideAssailant.class);
    }

    @Test
    void transformsToInfiltratorWhenItBecomesDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent infiltrator = castInfiltrator();

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(infiltrator.isTransformed()).isFalse();
        assertThat(infiltrator.getCard()).isInstanceOf(HarvesttideInfiltrator.class);
    }

    @Test
    void entersTransformedWhenItEntersDuringNight() {
        gd.dayNight = DayNight.NIGHT;
        castInfiltrator();

        Permanent infiltrator = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(infiltrator.isTransformed()).isTrue();
        assertThat(infiltrator.getCard()).isInstanceOf(HarvesttideAssailant.class);
    }


    @Test
    void frontFaceTramplesOverBlocker() {
        gd.dayNight = DayNight.DAY;
        Permanent attacker = addCreatureReady(player1, new HarvesttideInfiltrator());
        Permanent blocker = addCreatureReady(player2, new UnrulyMob());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Unruly Mob");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    void backFaceTramplesOverBlocker() {
        gd.dayNight = DayNight.NIGHT;
        Permanent attacker = castInfiltrator();
        attacker.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new UnrulyMob());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Unruly Mob");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    void opponentSpellsDoNotPreventNightAfterSpelllessActiveTurn() {
        gd.dayNight = DayNight.DAY;
        Permanent infiltrator = addCreatureReady(player1, new HarvesttideInfiltrator());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(infiltrator.getCard()).isInstanceOf(HarvesttideAssailant.class);
    }

    @Test
    void oneActivePlayerSpellKeepsDay() {
        gd.dayNight = DayNight.DAY;
        Permanent infiltrator = addCreatureReady(player1, new HarvesttideInfiltrator());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(infiltrator.isTransformed()).isFalse();
    }

    @Test
    void opponentSpellsDoNotCauseDayWhenActivePlayerCastsOnlyOne() {
        gd.dayNight = DayNight.NIGHT;
        Permanent infiltrator = castInfiltrator();
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(infiltrator.getCard()).isInstanceOf(HarvesttideAssailant.class);
    }

    private Permanent castInfiltrator() {
        harness.setHand(player1, List.of(new HarvesttideInfiltrator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

}
