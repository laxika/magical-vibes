package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RandomDiscardEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HypnoticSpecterTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Hypnotic Specter has correct card properties")
    void hasCorrectProperties() {
        HypnoticSpecter card = new HypnoticSpecter();

        assertThat(card.getName()).isEqualTo("Hypnotic Specter");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.SPECTER);
        assertThat(card.getKeywords()).contains(Keyword.FLYING);
        assertThat(card.getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER).getFirst()).isInstanceOf(RandomDiscardEffect.class);
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Dealing combat damage to player forces opponent to discard a card at random")
    void combatDamageTriggersRandomDiscard() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent specter = addReadyCreature(player1, new HypnoticSpecter());
        specter.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("discards") && log.contains("at random"));
    }

    @Test
    @DisplayName("Discards one card when opponent has multiple cards in hand")
    void discardsOneCardFromMultiple() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Permanent specter = addReadyCreature(player1, new HypnoticSpecter());
        specter.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("No trigger when opponent has empty hand")
    void noDiscardWhenEmptyHand() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of());

        Permanent specter = addReadyCreature(player1, new HypnoticSpecter());
        specter.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to discard"));
    }

    @Test
    @DisplayName("No trigger when Specter is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        Permanent specter = addReadyCreature(player1, new HypnoticSpecter());
        specter.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
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

        Permanent specter = addReadyCreature(player1, new HypnoticSpecter());
        specter.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Game advances after random discard trigger resolves")
    void gameAdvancesAfterTrigger() {
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent specter = addReadyCreature(player1, new HypnoticSpecter());
        specter.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Game log records the name of the discarded card")
    void gameLogRecordsDiscardedCardName() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent specter = addReadyCreature(player1, new HypnoticSpecter());
        specter.setAttacking(true);

        resolveCombat();

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Hypnotic Specter") && log.contains("Grizzly Bears") && log.contains("at random"));
    }
}

