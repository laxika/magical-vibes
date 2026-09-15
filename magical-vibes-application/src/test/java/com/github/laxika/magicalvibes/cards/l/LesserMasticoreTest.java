package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LesserMasticore.class, Forest.class, DoomBlade.class})
class LesserMasticoreTest extends BaseCardTest {

    @Test
    @DisplayName("Requires discarding a card to cast")
    void requiresDiscardToCast() {
        harness.setHand(player1, List.of(new LesserMasticore(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lesser Masticore");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot be cast without a card to discard")
    void cannotCastWithoutDiscard() {
        harness.setHand(player1, List.of(new LesserMasticore()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Deals 1 damage to target creature")
    void dealsDamageToTargetCreature() {
        Permanent masticore = addCreatureReady(player1, new LesserMasticore());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LesserMasticore());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(masticore.isTapped()).isFalse();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Persist returns it with a -1/-1 counter")
    void persistReturnsWithMinusOneMinusOneCounter() {
        Permanent masticore = harness.addToBattlefieldAndReturn(player1, new LesserMasticore());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player2, 0, masticore.getId());
        resolveUntilInputOrEmpty();

        Permanent returned = findPermanent(player1, "Lesser Masticore");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(returned.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("Persist does not return it when it died with a -1/-1 counter")
    void persistDoesNotReturnWithMinusOneMinusOneCounter() {
        Permanent masticore = harness.addToBattlefieldAndReturn(player1, new LesserMasticore());
        masticore.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player2, 0, masticore.getId());
        resolveUntilInputOrEmpty();

        harness.assertNotOnBattlefield(player1, "Lesser Masticore");
        harness.assertInGraveyard(player1, "Lesser Masticore");
    }

    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData gameData = harness.getGameData();
            if (gameData.interaction.isAwaitingInput() || gameData.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }
}
