package com.github.laxika.magicalvibes.cards.l;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.MammothSpider;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({LegionsEnd.class, GreenwoodSentinel.class, MammothSpider.class})
class LegionsEndTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target and same-name creatures, hand cards, and graveyard cards")
    void exilesMatchingCardsFromAllRequiredZones() {
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player2, List.of(new GreenwoodSentinel(), new MammothSpider()));
        harness.setGraveyard(player2, List.of(new GreenwoodSentinel(), new MammothSpider()));
        harness.setHand(player1, List.of(new LegionsEnd()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Greenwood Sentinel");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertNotInHand(player2, "Greenwood Sentinel");
        harness.assertInHand(player2, "Mammoth Spider");
        harness.assertNotInGraveyard(player2, "Greenwood Sentinel");
        harness.assertInGraveyard(player2, "Mammoth Spider");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Greenwood Sentinel"))
                .hasSize(4);
    }

    @Test
    @DisplayName("Cannot target a creature with mana value greater than two")
    void cannotTargetCreatureWithHighManaValue() {
        harness.addToBattlefield(player2, new MammothSpider());
        harness.setHand(player1, List.of(new LegionsEnd()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Mammoth Spider");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the caster")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new LegionsEnd()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Greenwood Sentinel");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Reveals the opponent's entire hand even when no hand cards match")
    void revealsHandWithoutMatchingCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setHand(player2, List.of(new MammothSpider()));
        harness.setHand(player1, List.of(new LegionsEnd()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.clearMessages();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertInHand(player2, "Mammoth Spider");
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .singleElement().asString().contains("Mammoth Spider");
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND"))
                .singleElement().asString().contains("Mammoth Spider");
    }

    @Test
    @DisplayName("An absent target prevents all exile and hand revelation")
    void absentTargetPreventsAllEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setHand(player2, List.of(new GreenwoodSentinel()));
        harness.setGraveyard(player2, List.of(new GreenwoodSentinel()));
        harness.setHand(player1, List.of(new LegionsEnd()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));
        harness.clearMessages();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(other);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
    }

    @Test
    @DisplayName("A face-down target shares no name with battlefield, hand, or graveyard cards")
    void faceDownTargetExilesOnlyItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MammothSpider());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent faceUp = harness.addToBattlefieldAndReturn(player2, new MammothSpider());
        Permanent faceDown = harness.addToBattlefieldAndReturn(player2, new MammothSpider());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player2, List.of(new MammothSpider()));
        harness.setGraveyard(player2, List.of(new MammothSpider()));
        harness.setHand(player1, List.of(new LegionsEnd()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(faceUp, faceDown);
        harness.assertInHand(player2, "Mammoth Spider");
        harness.assertInGraveyard(player2, "Mammoth Spider");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target.getCard());
    }

    @Test
    @DisplayName("A face-down creature does not share the face-up target's name")
    void faceDownSecondaryCreatureIsNotExiled() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent faceDown = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new LegionsEnd()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(faceDown);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target.getCard());
    }
}
