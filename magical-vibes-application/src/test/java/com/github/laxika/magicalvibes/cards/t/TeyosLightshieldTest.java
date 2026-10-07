package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeyosLightshield.class})
class TeyosLightshieldTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on a creature you control")
    void etbPutsCounterOnOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TeyosLightshield());

        harness.setHand(player1, List.of(new TeyosLightshield()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TeyosLightshield());
        harness.castFromHand(player1, new TeyosLightshield(), "{2}{W}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, findPermanent(player1, "Teyo's Lightshield").getId());
        resolveAllTriggers();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The entering Lightshield can target itself even on an empty battlefield")
    void canTargetItself() {
        harness.castFromHand(player1, new TeyosLightshield(), "{2}{W}");
        harness.passBothPriorities();
        Permanent lightshield = findPermanent(player1, "Teyo's Lightshield");

        harness.handlePermanentChosen(player1, lightshield.getId());
        resolveAllTriggers();

        assertThat(lightshield.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast still puts a counter on a controlled creature")
    void triggersWhenNotCast() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TeyosLightshield());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new TeyosLightshield());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger does not put a counter on a target that has left the battlefield")
    void targetLeavingBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TeyosLightshield());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new TeyosLightshield());
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger still resolves when its source has left the battlefield")
    void resolvesWithoutSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TeyosLightshield());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new TeyosLightshield());
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
