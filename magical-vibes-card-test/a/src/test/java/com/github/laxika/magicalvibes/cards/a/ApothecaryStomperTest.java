package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ApothecaryStomper.class})
class ApothecaryStomperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mode puts two +1/+1 counters on a creature you control")
    void etbPutsTwoCountersOnOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ApothecaryStomper());

        enterWithCounterMode(target.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("ETB mode gains 4 life")
    void etbGainsFourLife() {
        harness.setLife(player1, 5);

        castStomper();
        harness.handleListChoice(player1, "You gain 4 life");
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("Counter mode cannot target an opponent's creature")
    void counterModeCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ApothecaryStomper());

        Permanent stomper = castStomper();
        harness.handleListChoice(player1, "Put two +1/+1 counters on target creature you control");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, stomper.getId());
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(stomper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mode is chosen after the spell resolves and can target the entering creature")
    void choosesModeAfterResolvingAndCanTargetItself() {
        harness.castFromHand(player1, new ApothecaryStomper(), "{4}{G}{G}");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        Permanent stomper = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handleListChoice(player1, "Put two +1/+1 counters on target creature you control");
        harness.handlePermanentChosen(player1, stomper.getId());
        harness.passBothPriorities();

        assertThat(stomper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Life gain resolves even after its source leaves the battlefield")
    void lifeGainResolvesAfterSourceLeaves() {
        harness.setLife(player1, 5);
        Permanent stomper = castStomper();
        harness.handleListChoice(player1, "You gain 4 life");
        gd.playerBattlefields.get(player1.getId()).remove(stomper);
        gd.playerGraveyards.get(player1.getId()).add(stomper.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(9);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void enterWithCounterMode(UUID targetId) {
        castStomper();
        harness.handleListChoice(player1, "Put two +1/+1 counters on target creature you control");
        harness.handlePermanentChosen(player1, targetId);
    }

    private Permanent castStomper() {
        harness.castFromHand(player1, new ApothecaryStomper(), "{4}{G}{G}");
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getLast();
    }
}
