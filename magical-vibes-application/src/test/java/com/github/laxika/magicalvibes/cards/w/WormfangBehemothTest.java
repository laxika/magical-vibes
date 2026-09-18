package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BorderPatrol;
import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WormfangBehemoth.class, BorderPatrol.class, GiantWarthog.class})
class WormfangBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("The enters-the-battlefield ability exiles the controller's hand with Wormfang Behemoth")
    void entersTheBattlefieldExilesControllerHand() {
        Card first = new BorderPatrol();
        Card second = new GiantWarthog();
        Permanent behemoth = castBehemoth(first, second);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .filteredOn(ExiledCardEntry::sourcePermanentId, behemoth.getId())
                .extracting(ExiledCardEntry::card)
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.exiledCards)
                .filteredOn(ExiledCardEntry::sourcePermanentId, behemoth.getId())
                .allMatch(entry -> !entry.faceDown());
    }

    @Test
    @DisplayName("The leaves-the-battlefield ability returns the cards to their owners' hands")
    void leavesTheBattlefieldReturnsExiledCards() {
        Card first = new BorderPatrol();
        Card second = new GiantWarthog();
        Card unrelated = new GiantWarthog();
        harness.setExile(player1, List.of(unrelated));
        Permanent behemoth = castBehemoth(first, second);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, behemoth));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.exiledCards)
                .noneMatch(entry -> behemoth.getId().equals(entry.sourcePermanentId()));
        assertThat(gd.findExiledCard(unrelated.getId())).isNotNull();
        assertThat(gd.findExiledCard(unrelated.getId()).sourcePermanentId()).isNull();
    }

    @Test
    @DisplayName("The enters-the-battlefield ability exiles the hand when its trigger resolves")
    void entersTheBattlefieldUsesHandAtResolution() {
        Card late = new BorderPatrol();
        harness.setHand(player1, List.of(new WormfangBehemoth()));
        addBehemothMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent behemoth = findPermanent(player1, "Wormfang Behemoth");
        harness.setHand(player1, List.of(late));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(behemoth.getId())).containsExactly(late);
    }

    @Test
    @DisplayName("The leaves-the-battlefield ability returns cards when the source is exiled")
    void leavesTheBattlefieldReturnsExiledCardsFromAnyDestination() {
        Card first = new BorderPatrol();
        Card second = new GiantWarthog();
        Permanent behemoth = castBehemoth(first, second);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, behemoth));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.findExiledCard(behemoth.getCard().getId())).isNotNull();
        assertThat(gd.getCardsExiledByPermanent(behemoth.getId())).isEmpty();
    }

    @Test
    @DisplayName("The leaves-the-battlefield ability returns each card to its owner's hand")
    void leavesTheBattlefieldReturnsCardsToTheirOwnersHands() {
        Card opponentOwned = new BorderPatrol();
        opponentOwned.setOwnerId(player2.getId());
        harness.setHand(player2, List.of());
        Permanent behemoth = castBehemoth(opponentOwned);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, behemoth));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentOwned);
    }

    @Test
    @DisplayName("If Wormfang Behemoth leaves before its enters-the-battlefield ability resolves, the cards remain exiled")
    void leavingBeforeEnterTriggerResolvesLeavesCardsExiled() {
        Card first = new BorderPatrol();
        Card second = new GiantWarthog();
        harness.setHand(player1, new ArrayList<>(List.of(new WormfangBehemoth(), first, second)));
        addBehemothMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent behemoth = findPermanent(player1, "Wormfang Behemoth");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, behemoth));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .filteredOn(ExiledCardEntry::sourcePermanentId, behemoth.getId())
                .extracting(ExiledCardEntry::card)
                .containsExactlyInAnyOrder(first, second);
    }

    private Permanent castBehemoth(Card... handCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(new WormfangBehemoth());
        hand.addAll(List.of(handCards));
        harness.setHand(player1, hand);
        addBehemothMana();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Wormfang Behemoth");
    }

    private void addBehemothMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
