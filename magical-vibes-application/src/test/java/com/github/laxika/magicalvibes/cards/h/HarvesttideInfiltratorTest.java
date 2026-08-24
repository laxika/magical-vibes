package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarvesttideInfiltrator.class, HarvesttideAssailant.class})
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
        advanceToUntap(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(infiltrator.isTransformed()).isTrue();
        assertThat(infiltrator.getCard()).isInstanceOf(HarvesttideAssailant.class);
    }

    @Test
    void transformsToInfiltratorWhenItBecomesDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent infiltrator = castInfiltrator();

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        advanceToUntap(player1);

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

    private Permanent castInfiltrator() {
        harness.setHand(player1, List.of(new HarvesttideInfiltrator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void advanceToUntap(Player activePlayer) {
        harness.performUntapStep(activePlayer);
    }
}
