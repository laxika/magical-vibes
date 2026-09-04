package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HypnoticSpecter.class, GrizzlyBears.class})
class HypnoticSpecterTest extends BaseCardTest {

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Dealing combat damage to player forces opponent to discard a card at random")
    void combatDamageTriggersRandomDiscard() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent specter = addCreatureReady(player1, new HypnoticSpecter());
        specter.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("discards") && log.contains("at random"));
    }

    @Test
    @DisplayName("Discards one card when opponent has multiple cards in hand")
    void discardsOneCardFromMultiple() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Permanent specter = addCreatureReady(player1, new HypnoticSpecter());
        specter.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("No card is discarded when opponent has empty hand")
    void noDiscardWhenEmptyHand() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of());

        Permanent specter = addCreatureReady(player1, new HypnoticSpecter());
        specter.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to discard"));
    }

    @Test
    @DisplayName("No trigger when Specter is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        Permanent specter = addCreatureReady(player1, new HypnoticSpecter());
        specter.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Defender takes 2 combat damage from unblocked Specter")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent specter = addCreatureReady(player1, new HypnoticSpecter());
        specter.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Game advances after random discard trigger resolves")
    void gameAdvancesAfterTrigger() {
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent specter = addCreatureReady(player1, new HypnoticSpecter());
        specter.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Game log records the name of the discarded card")
    void gameLogRecordsDiscardedCardName() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent specter = addCreatureReady(player1, new HypnoticSpecter());
        specter.setAttacking(true);

        resolveCombat();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Grizzly Bears") && log.contains("at random"));
    }

    @Test
    @DisplayName("Noncombat damage to an opponent also triggers random discard")
    void noncombatDamageTriggersRandomDiscard() {
        GameData gd = harness.getGameData();
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent specter = addCreatureReady(player1, new HypnoticSpecter());
        DamageSupport damageSupport = GameTestEngineContext.get().getBean(DamageSupport.class);
        harness.inMutationScope(() -> damageSupport.dealDividedDamageToAnyTargets(
                gd, specter.getCard(), player1.getId(), Map.of(player2.getId(), 1)));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
