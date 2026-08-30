package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WormfangBehemoth.class, GrizzlyBears.class, LlanowarElves.class})
class WormfangBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("The enters-the-battlefield ability exiles the controller's hand with Wormfang Behemoth")
    void entersTheBattlefieldExilesControllerHand() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
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
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Permanent behemoth = castBehemoth(first, second);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, behemoth));
        resolvePendingTrigger();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.exiledCards)
                .noneMatch(entry -> behemoth.getId().equals(entry.sourcePermanentId()));
    }

    @Test
    @DisplayName("If Wormfang Behemoth leaves before its enters-the-battlefield ability resolves, the cards remain exiled")
    void leavingBeforeEnterTriggerResolvesLeavesCardsExiled() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setHand(player1, new ArrayList<>(List.of(new WormfangBehemoth(), first, second)));
        addBehemothMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent behemoth = findPermanent(player1, "Wormfang Behemoth");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, behemoth));
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Wormfang Behemoth");
    }

    private void addBehemothMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void resolvePendingTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
