package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefenderOfTheOrder.class, AvenEnvoy.class})
class DefenderOfTheOrderTest extends BaseCardTest {

    @Test
    void turningFaceUpBoostsYourCreaturesUntilEndOfTurn() {
        Permanent ownEnvoy = harness.addToBattlefieldAndReturn(player1, new AvenEnvoy());
        Permanent opposingEnvoy = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());
        Permanent defender = castFaceDown();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(defender));
        harness.passBothPriorities();

        assertThat(ownEnvoy.getEffectivePower()).isEqualTo(0);
        assertThat(ownEnvoy.getEffectiveToughness()).isEqualTo(4);
        assertThat(defender.getEffectivePower()).isEqualTo(2);
        assertThat(defender.getEffectiveToughness()).isEqualTo(6);
        assertThat(opposingEnvoy.getEffectivePower()).isEqualTo(0);
        assertThat(opposingEnvoy.getEffectiveToughness()).isEqualTo(2);

        Permanent laterEnvoy = harness.addToBattlefieldAndReturn(player1, new AvenEnvoy());
        assertThat(laterEnvoy.getEffectivePower()).isEqualTo(0);
        assertThat(laterEnvoy.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownEnvoy.getEffectivePower()).isEqualTo(0);
        assertThat(ownEnvoy.getEffectiveToughness()).isEqualTo(2);
        assertThat(defender.getEffectivePower()).isEqualTo(2);
        assertThat(defender.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void requiresBothWhiteManaToTurnFaceUp() {
        Permanent defender = castFaceDown();

        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(defender)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(defender.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(defender));
        harness.passBothPriorities();

        assertThat(defender.isFaceDown()).isFalse();
    }

    @Test
    void castingFaceUpDoesNotTriggerTurnFaceUpAbility() {
        Permanent ownEnvoy = harness.addToBattlefieldAndReturn(player1, new AvenEnvoy());
        Permanent opposingEnvoy = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());

        harness.castFromHand(player1, new DefenderOfTheOrder(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(ownEnvoy.getEffectivePower()).isEqualTo(0);
        assertThat(ownEnvoy.getEffectiveToughness()).isEqualTo(2);
        assertThat(opposingEnvoy.getEffectivePower()).isEqualTo(0);
        assertThat(opposingEnvoy.getEffectiveToughness()).isEqualTo(2);
        assertThat(findPermanent(player1, "Defender of the Order").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player1, "Defender of the Order").getEffectiveToughness()).isEqualTo(4);
    }

    private Permanent castFaceDown() {
        DefenderOfTheOrder card = new DefenderOfTheOrder();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
