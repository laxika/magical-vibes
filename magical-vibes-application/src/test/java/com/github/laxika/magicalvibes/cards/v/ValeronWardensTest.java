package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TopanFreeblade;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValeronWardensTest extends BaseCardTest {

    @Test
    @DisplayName("Renown 2 puts two counters on the Wardens and its own renown draws a card")
    void ownRenownDrawsACard() {
        Permanent wardens = addCreatureReady(player1, new ValeronWardens());
        setupLibrary();
        int handBefore = handSize();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(wardens.isRenowned()).isTrue();
        assertThat(wardens.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(handSize()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Another creature you control becoming renowned draws a card")
    void allyRenownDrawsACard() {
        Permanent freeblade = addCreatureReady(player1, new TopanFreeblade());
        Permanent wardens = addCreatureReady(player1, new ValeronWardens());
        setupLibrary();
        int handBefore = handSize();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(freeblade.isRenowned()).isTrue();
        assertThat(wardens.isRenowned()).isFalse();
        assertThat(handSize()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("An already renowned creature becoming nothing draws no card")
    void alreadyRenownedDrawsNothing() {
        Permanent wardens = addCreatureReady(player1, new ValeronWardens());
        wardens.setRenowned(true);
        setupLibrary();
        int handBefore = handSize();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(wardens.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(handSize()).isEqualTo(handBefore);
    }

    @Test
    @DisplayName("A blocked Wardens never becomes renowned, so no card is drawn")
    void blockedDrawsNothing() {
        Permanent wardens = addCreatureReady(player1, new ValeronWardens());
        addCreatureReady(player2, new WallOfWood());
        setupLibrary();
        int handBefore = handSize();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(wardens.isRenowned()).isFalse();
        assertThat(handSize()).isEqualTo(handBefore);
    }

    @Test
    @DisplayName("An opponent's creature becoming renowned does not draw for the Wardens' controller")
    void opponentRenownDrawsNothing() {
        addCreatureReady(player1, new ValeronWardens());
        Permanent freeblade = addCreatureReady(player2, new TopanFreeblade());
        setupLibrary();
        int handBefore = handSize();

        harness.forceActivePlayer(player2);
        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        // player2 is the attacker here, and player1's Wardens is a possible blocker, so the combat
        // needs an explicit "no blocks" for the Freeblade to connect. Do not pass priority again
        // afterwards: combat damage is already done, and another pass would roll into player1's
        // turn and draw them a card, masking the very draw this test is asserting the absence of.
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        resolveAllTriggers();

        assertThat(freeblade.isRenowned()).isTrue();
        assertThat(handSize()).isEqualTo(handBefore);
    }

    private int handSize() {
        return harness.getGameData().playerHands.get(player1.getId()).size();
    }

    private void setupLibrary() {
        GameData gameData = harness.getGameData();
        for (var deck : List.of(gameData.playerDecks.get(player1.getId()), gameData.playerDecks.get(player2.getId()))) {
            deck.clear();
            deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));
        }
    }
}
