package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.w.FearsomeWerewolf;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FearfulVillager.class, FearsomeWerewolf.class})
class FearfulVillagerTest extends BaseCardTest {

    @Test
    void becomesDayWhenItEntersWithoutADesignation() {
        Permanent villager = harness.enterBattlefieldAndReturn(player1, new FearfulVillager());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(villager.isTransformed()).isFalse();
        assertThat(villager.getCard()).isInstanceOf(FearfulVillager.class);
    }

    @Test
    void entersAsFearsomeWerewolfDuringNight() {
        gd.dayNight = DayNight.NIGHT;
        Permanent villager = harness.enterBattlefieldAndReturn(player1, new FearfulVillager());

        assertThat(villager.isTransformed()).isTrue();
        assertThat(villager.getCard()).isInstanceOf(FearsomeWerewolf.class);
    }

    @Test
    void transformsWithDayAndNight() {
        gd.dayNight = DayNight.DAY;
        Permanent villager = harness.enterBattlefieldAndReturn(player1, new FearfulVillager());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(villager.isTransformed()).isTrue();
        assertThat(villager.getCard()).isInstanceOf(FearsomeWerewolf.class);

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(villager.isTransformed()).isFalse();
        assertThat(villager.getCard()).isInstanceOf(FearfulVillager.class);
    }

    @ParameterizedTest
    @EnumSource(value = DayNight.class, names = {"DAY", "NIGHT"})
    void menaceRejectsOneBlockerOnEitherFace(DayNight designation) {
        gd.dayNight = designation;
        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new FearfulVillager());
        attacker.setSummoningSick(false);
        harness.enterBattlefieldAndReturn(player2, new FearfulVillager());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @ParameterizedTest
    @EnumSource(value = DayNight.class, names = {"DAY", "NIGHT"})
    void menaceAllowsTwoBlockersOnEitherFace(DayNight designation) {
        gd.dayNight = designation;
        Permanent attacker = harness.enterBattlefieldAndReturn(player1, new FearfulVillager());
        attacker.setSummoningSick(false);
        Permanent first = harness.enterBattlefieldAndReturn(player2, new FearfulVillager());
        Permanent second = harness.enterBattlefieldAndReturn(player2, new FearfulVillager());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void opponentSpellsDoNotPreventNightAfterSpelllessActiveTurn() {
        Permanent villager = harness.enterBattlefieldAndReturn(player1, new FearfulVillager());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(villager.isTransformed()).isTrue();
    }

    @Test
    void opponentSpellsDoNotCauseDayWhenActivePlayerCastOnlyOne() {
        gd.dayNight = DayNight.NIGHT;
        Permanent villager = harness.enterBattlefieldAndReturn(player1, new FearfulVillager());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(villager.isTransformed()).isTrue();
    }

    @Test
    void oneSpellDuringActiveTurnKeepsDay() {
        Permanent villager = harness.enterBattlefieldAndReturn(player1, new FearfulVillager());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(villager.isTransformed()).isFalse();
    }
}
