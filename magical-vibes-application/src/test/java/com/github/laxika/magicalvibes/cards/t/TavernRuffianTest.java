package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TavernRuffian.class, TavernSmasher.class})
class TavernRuffianTest extends BaseCardTest {

    @Test
    void transformsToTavernSmasherWhenNoSpellsWereCastLastTurn() {
        gd.dayNight = DayNight.DAY;
        Permanent ruffian = harness.enterBattlefieldAndReturn(player1, new TavernRuffian());
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolveTransform(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(ruffian.isTransformed()).isTrue();
        assertThat(ruffian.getCard()).isInstanceOf(TavernSmasher.class);
    }

    @Test
    void transformsToTavernRuffianWhenTwoSpellsWereCastLastTurn() {
        gd.dayNight = DayNight.NIGHT;
        Permanent ruffian = harness.enterBattlefieldAndReturn(player1, new TavernRuffian());
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        advanceToUpkeepAndResolveTransform(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(ruffian.isTransformed()).isFalse();
        assertThat(ruffian.getCard()).isInstanceOf(TavernRuffian.class);
    }

    @Test
    void doesNotTransformBackWhenOnlyOneSpellWasCastLastTurn() {
        gd.dayNight = DayNight.NIGHT;
        Permanent ruffian = harness.enterBattlefieldAndReturn(player1, new TavernRuffian());
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.performUntapStep(player1);

        assertThat(ruffian.isTransformed()).isTrue();
    }

    private void advanceToUpkeepAndResolveTransform(Player activePlayer) {
        harness.performUntapStep(activePlayer);
    }
}
