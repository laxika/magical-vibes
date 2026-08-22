package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheElderspell.class, GarrukWildspeaker.class, GideonBlackblade.class, GrizzlyBears.class})
class TheElderspellTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys any number of target planeswalkers and adds two loyalty counters for each")
    void destroysTargetPlaneswalkersAndAddsLoyaltyCounters() {
        Permanent ownPlaneswalker = addPlaneswalker(player1, new GarrukWildspeaker(), 3);
        Permanent opponentGarruk = addPlaneswalker(player2, new GarrukWildspeaker(), 5);
        Permanent opponentGideon = addPlaneswalker(player2, new GideonBlackblade(), 4);

        castTheElderspell(List.of(opponentGarruk.getId(), opponentGideon.getId()));

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertNotOnBattlefield(player2, "Gideon Blackblade");
        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("Resolves with no targets and adds no loyalty counters")
    void resolvesWithNoTargets() {
        Permanent ownPlaneswalker = addPlaneswalker(player1, new GarrukWildspeaker(), 3);

        castTheElderspell(List.of());

        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a non-planeswalker permanent")
    void cannotTargetNonPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheElderspell()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a planeswalker");
    }

    private void castTheElderspell(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new TheElderspell()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private Permanent addPlaneswalker(Player player, Card card, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        return planeswalker;
    }
}
