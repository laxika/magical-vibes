package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonMage.class, ScornfulEgotist.class})
class DragonMageTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes each player discard their hand and draw seven cards")
    void combatDamageWheelsBothHandsIntoSevenCards() {
        Permanent dragonMage = addCreatureReady(player1, new DragonMage());
        dragonMage.setAttacking(true);
        harness.setHand(player1, List.of(new ScornfulEgotist(), new ScornfulEgotist()));
        harness.setHand(player2, List.of(new ScornfulEgotist()));
        harness.setLibrary(player1, sevenScornfulEgotists());
        harness.setLibrary(player2, sevenScornfulEgotists());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No combat damage means no discard or draw")
    void noCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new DragonMage());
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.setHand(player2, List.of(new ScornfulEgotist()));
        harness.setLibrary(player1, sevenScornfulEgotists());
        harness.setLibrary(player2, sevenScornfulEgotists());

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage still makes players draw seven cards with empty hands")
    void combatDamageDrawsSevenCardsEvenWhenHandsAreEmpty() {
        Permanent dragonMage = addCreatureReady(player1, new DragonMage());
        dragonMage.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, sevenScornfulEgotists());
        harness.setLibrary(player2, sevenScornfulEgotists());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not wheel either hand")
    void damageToBlockerDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new DragonMage());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DragonMage());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.setHand(player2, List.of(new ScornfulEgotist()));
        harness.setLibrary(player1, sevenScornfulEgotists());
        harness.setLibrary(player2, sevenScornfulEgotists());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("The wheel resolves even after Dragon Mage leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent dragonMage = addCreatureReady(player1, new DragonMage());
        dragonMage.setAttacking(true);
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.setHand(player2, List.of(new ScornfulEgotist()));
        harness.setLibrary(player1, sevenScornfulEgotists());
        harness.setLibrary(player2, sevenScornfulEgotists());

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(dragonMage);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The active player draws all seven cards before the other player")
    void secondPlayerAttackingDrawsFirst() {
        Permanent dragonMage = addCreatureReady(player2, new DragonMage());
        dragonMage.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, sevenScornfulEgotists());
        harness.setLibrary(player2, sevenScornfulEgotists());

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText())
                .filter(text -> text.endsWith(" draws a card.")).toList())
                .containsExactlyElementsOf(java.util.stream.Stream.concat(
                        java.util.Collections.nCopies(7, gd.playerIdToName.get(player2.getId()) + " draws a card.").stream(),
                        java.util.Collections.nCopies(7, gd.playerIdToName.get(player1.getId()) + " draws a card.").stream()).toList());
    }

    private List<Card> sevenScornfulEgotists() {
        return List.of(
                new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist(),
                new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist());
    }
}
