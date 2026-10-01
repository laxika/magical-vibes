package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.t.TransguildCourier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NovijenHeartOfProgress.class, TransguildCourier.class, MistralCharger.class})
class NovijenHeartOfProgressTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorless() {
        Permanent novijen = addReadyNovijen();

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gameData.stack).isEmpty();
        assertThat(novijen.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Puts counters on every creature that entered the battlefield this turn")
    void putsCountersOnCreaturesThatEnteredThisTurn() {
        Permanent novijen = addReadyNovijen();
        Permanent oldCreature = harness.addToBattlefieldAndReturn(player1, new TransguildCourier());
        Permanent recentOwnCreature = harness.enterBattlefieldAndReturn(player1, new TransguildCourier());
        Permanent recentOpposingCreature = harness.enterBattlefieldAndReturn(player2, new MistralCharger());
        Permanent recentLand = harness.enterBattlefieldAndReturn(player2, new NovijenHeartOfProgress());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(novijen.isTapped()).isTrue();
        assertThat(oldCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(recentOwnCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(recentOpposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(recentLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyNovijen() {
        Permanent novijen = harness.addToBattlefieldAndReturn(player1, new NovijenHeartOfProgress());
        novijen.setSummoningSick(false);
        return novijen;
    }
}
