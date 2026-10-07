package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
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

@CardUsed({StolenGrain.class, ShuFootSoldiers.class, GarrukWildspeaker.class, SafePassage.class})
class StolenGrainTest extends BaseCardTest {

    private void addManaForStolenGrain() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Deals 5 damage to target opponent and controller gains 5 life")
    void dealsDamageAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new StolenGrain()));
        addManaForStolenGrain();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 25);
    }

    @Test
    @CardUsed(GarrukWildspeaker.class)
    @DisplayName("Deals 5 damage to a target planeswalker and controller gains 5 life")
    void dealsDamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        harness.setHand(player1, List.of(new StolenGrain()));
        addManaForStolenGrain();

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 25);
    }

    @Test
    @CardUsed(GarrukWildspeaker.class)
    @DisplayName("Can target a planeswalker controlled by the caster")
    void canTargetOwnPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        harness.setHand(player1, List.of(new StolenGrain()));
        addManaForStolenGrain();

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Cannot target yourself — only an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new StolenGrain()));
        addManaForStolenGrain();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature — only opponent or planeswalker")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());

        harness.setHand(player1, List.of(new StolenGrain()));
        addManaForStolenGrain();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains 5 life even when all damage is prevented")
    void gainsLifeWhenDamageIsPrevented() {
        harness.setHand(player1, List.of(new StolenGrain()));
        addManaForStolenGrain();
        harness.castSorcery(player1, 0, player2.getId());

        harness.setHand(player2, List.of(new SafePassage()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 25);
        harness.assertInGraveyard(player1, "Stolen Grain");
    }

    @Test
    @DisplayName("Does not gain life when the only target leaves before resolution")
    void doesNotGainLifeWhenTargetLeaves() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        harness.setHand(player1, List.of(new StolenGrain()));
        addManaForStolenGrain();
        harness.castSorcery(player1, 0, planeswalker.getId());

        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        harness.setGraveyard(player2, List.of(planeswalker.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Stolen Grain");
    }
}
