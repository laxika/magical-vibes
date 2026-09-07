package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.w.FearsomeWerewolf;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        advanceToUntap(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(villager.isTransformed()).isTrue();
        assertThat(villager.getCard()).isInstanceOf(FearsomeWerewolf.class);

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        advanceToUntap(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(villager.isTransformed()).isFalse();
        assertThat(villager.getCard()).isInstanceOf(FearfulVillager.class);
    }

    private void advanceToUntap(Player activePlayer) {
        harness.performUntapStep(activePlayer);
    }
}
