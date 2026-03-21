package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KarnsTemporalSunderingTest extends BaseCardTest {

    private void enableAutoStop() {
        GameData gd = harness.getGameData();
        Set<TurnStep> stops1 = ConcurrentHashMap.newKeySet();
        stops1.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player1.getId(), stops1);
        Set<TurnStep> stops2 = ConcurrentHashMap.newKeySet();
        stops2.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player2.getId(), stops2);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has correct effects: extra turn, bounce, exile self")
    void hasCorrectProperties() {
        KarnsTemporalSundering card = new KarnsTemporalSundering();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getMinTargets()).isEqualTo(1);
        assertThat(card.getMaxTargets()).isEqualTo(2);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ExtraTurnEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ReturnTargetPermanentToHandEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(2)).isInstanceOf(ExileSpellEffect.class);
    }

    // ===== Legendary sorcery restriction =====

    @Test
    @DisplayName("Cannot cast without controlling a legendary creature or planeswalker")
    void cannotCastWithoutLegendaryPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast when controlling a legendary creature")
    void canCastWithLegendaryCreature() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, List.of(player1.getId()));

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getCard().getName())
                .isEqualTo("Karn's Temporal Sundering");
    }

    // ===== Extra turn =====

    @Test
    @DisplayName("Grants an extra turn to the targeted player")
    void grantsExtraTurnToTargetedPlayer() {
        enableAutoStop();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, List.of(player1.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.extraTurns).hasSize(1);
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Extra turn is taken after current turn ends")
    void extraTurnIsTakenAfterCurrentTurnEnds() {
        enableAutoStop();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        int turnBefore = gd.turnNumber;

        harness.castSorcery(player1, 0, List.of(player1.getId()));
        harness.passBothPriorities();

        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Can target opponent for extra turn")
    void canTargetOpponentForExtraTurn() {
        enableAutoStop();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.extraTurns).containsExactly(player2.getId());
    }

    // ===== Bounce =====

    @Test
    @DisplayName("Returns target nonland permanent to its owner's hand")
    void returnsNonlandPermanentToHand() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, List.of(player1.getId(), bearsId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can be cast with only a player target (no permanent target)")
    void canCastWithOnlyPlayerTarget() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, List.of(player1.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Should still resolve — extra turn granted, no bounce
        assertThat(gd.stack).isEmpty();
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Bounce target is removed before resolution — extra turn still resolves")
    void bounceTargetRemovedBeforeResolution() {
        enableAutoStop();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, List.of(player1.getId(), bearsId));

        // Remove bears before resolution
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Extra turn still granted even though bounce target is gone
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    // ===== Exile =====

    @Test
    @DisplayName("Is exiled after resolution instead of going to graveyard")
    void isExiledAfterResolution() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, List.of(player1.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Karn's Temporal Sundering"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Karn's Temporal Sundering"));
    }

    // ===== Combined behavior =====

    @Test
    @DisplayName("Extra turn + bounce + exile all resolve correctly together")
    void allEffectsResolveTogether() {
        enableAutoStop();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, List.of(player1.getId(), bearsId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Extra turn granted
        assertThat(gd.extraTurns).containsExactly(player1.getId());

        // Bears bounced to hand
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Spell exiled
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Karn's Temporal Sundering"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Karn's Temporal Sundering"));
    }
}
