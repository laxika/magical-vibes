package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AshiokDreamRender;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
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

@CardUsed({TheElderspell.class, GarrukWildspeaker.class, GideonBlackblade.class, GrizzlyBears.class,
        NarsetParterOfVeils.class, AshiokDreamRender.class, RestInPeace.class})
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

    @Test
    void choosesOnlyOneSurvivingPlaneswalkerForAllCounters() {
        Permanent narset = addPlaneswalker(player1, new NarsetParterOfVeils(), 5);
        Permanent ashiok = addPlaneswalker(player1, new AshiokDreamRender(), 5);
        Permanent opponent = addPlaneswalker(player2, new GideonBlackblade(), 4);

        castTheElderspell(List.of(opponent.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(ashiok.getId()));

        harness.assertNotOnBattlefield(player2, "Gideon Blackblade");
        assertThat(narset.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    void canDestroyOwnPlaneswalkerAndGiveCountersToAnother() {
        Permanent recipient = addPlaneswalker(player1, new NarsetParterOfVeils(), 5);
        Permanent destroyed = addPlaneswalker(player1, new AshiokDreamRender(), 5);

        castTheElderspell(List.of(destroyed.getId()));

        harness.assertInGraveyard(player1, "Ashiok, Dream Render");
        assertThat(recipient.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    void destroysPlaneswalkersWithoutControllingARecipient() {
        Permanent opponent = addPlaneswalker(player2, new NarsetParterOfVeils(), 5);

        castTheElderspell(List.of(opponent.getId()));

        harness.assertNotOnBattlefield(player2, "Narset, Parter of Veils");
        harness.assertInGraveyard(player2, "Narset, Parter of Veils");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void indestructiblePlaneswalkerDoesNotContributeLoyalty() {
        Permanent gideon = addPlaneswalker(player1, new GideonBlackblade(), 4);
        Permanent opponent = addPlaneswalker(player2, new NarsetParterOfVeils(), 5);

        castTheElderspell(List.of(gideon.getId(), opponent.getId()));

        harness.assertOnBattlefield(player1, "Gideon Blackblade");
        harness.assertInGraveyard(player2, "Narset, Parter of Veils");
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void destroyedPlaneswalkerStillContributesLoyaltyWhenExiledInstead() {
        harness.addToBattlefield(player1, new RestInPeace());
        Permanent recipient = addPlaneswalker(player1, new NarsetParterOfVeils(), 5);
        Permanent opponent = addPlaneswalker(player2, new AshiokDreamRender(), 5);

        castTheElderspell(List.of(opponent.getId()));

        harness.assertNotOnBattlefield(player2, "Ashiok, Dream Render");
        harness.assertNotInGraveyard(player2, "Ashiok, Dream Render");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(opponent.getCard().getId()));
        assertThat(recipient.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    void countsOnlyTargetsStillPresentAtResolution() {
        Permanent recipient = addPlaneswalker(player1, new NarsetParterOfVeils(), 5);
        Permanent removed = addPlaneswalker(player2, new AshiokDreamRender(), 5);
        Permanent remaining = addPlaneswalker(player2, new GideonBlackblade(), 4);
        harness.setHand(player1, List.of(new TheElderspell()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, List.of(removed.getId(), remaining.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, removed));

        harness.passBothPriorities();

        harness.assertInHand(player2, "Ashiok, Dream Render");
        harness.assertInGraveyard(player2, "Gideon Blackblade");
        assertThat(recipient.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    void doesNotResolveWhenEveryTargetHasLeftTheBattlefield() {
        Permanent recipient = addPlaneswalker(player1, new NarsetParterOfVeils(), 5);
        Permanent opponent = addPlaneswalker(player2, new AshiokDreamRender(), 5);
        harness.setHand(player1, List.of(new TheElderspell()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, List.of(opponent.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, opponent));

        harness.passBothPriorities();

        harness.assertInHand(player2, "Ashiok, Dream Render");
        harness.assertInGraveyard(player1, "The Elderspell");
        assertThat(recipient.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castTheElderspell(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new TheElderspell()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }

    private Permanent addPlaneswalker(Player player, Card card, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        return planeswalker;
    }
}
