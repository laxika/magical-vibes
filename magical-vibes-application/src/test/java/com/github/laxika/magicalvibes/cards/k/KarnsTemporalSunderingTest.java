package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.cards.u.Unwind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarnsTemporalSundering.class, ArvadTheCursed.class, BalothGorger.class,
        KarnScionOfUrza.class, MoxAmber.class, Island.class, Unwind.class})
class KarnsTemporalSunderingTest extends BaseCardTest {

    @Test
    void counteredSpellGoesToGraveyardWithoutGrantingExtraTurn() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.setHand(player2, List.of(new Unwind()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, List.of(player1.getId()));
        UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.castInstant(player2, 0, spellId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Karn's Temporal Sundering");
        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c instanceof KarnsTemporalSundering);
    }

    @Test
    void canCastWithLegendaryPlaneswalker() {
        harness.addToBattlefield(player1, new KarnScionOfUrza());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId()));

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c instanceof KarnsTemporalSundering);
    }

    @Test
    void legendaryArtifactDoesNotPermitCasting() {
        harness.addToBattlefield(player1, new MoxAmber());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void opponentsLegendaryCreatureDoesNotPermitCasting() {
        harness.addToBattlefield(player2, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void cannotChooseLandForBounce() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        UUID landId = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player1.getId(), landId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBounceOwnOnlyLegendaryCreature() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        UUID arvadId = harness.getPermanentId(player1, "Arvad the Cursed");

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), arvadId));

        harness.assertNotOnBattlefield(player1, "Arvad the Cursed");
        harness.assertInHand(player1, "Arvad the Cursed");
        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c instanceof KarnsTemporalSundering);
    }

    @Test
    void canBounceNoncreatureArtifact() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new MoxAmber());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        UUID artifactId = harness.getPermanentId(player2, "Mox Amber");

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), artifactId));

        harness.assertNotOnBattlefield(player2, "Mox Amber");
        harness.assertInHand(player2, "Mox Amber");
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void losingLegendaryCreatureAfterCastingDoesNotStopResolution() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, List.of(player1.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c instanceof KarnsTemporalSundering);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot cast without controlling a legendary creature or planeswalker")
    void cannotCastWithoutLegendaryPermanent() {
        harness.addToBattlefield(player1, new BalothGorger());
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

    @Test
    @DisplayName("Grants an extra turn to the targeted player")
    void grantsExtraTurnToTargetedPlayer() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.addToBattlefield(player1, new ArvadTheCursed());
            harness.setHand(player1, List.of(new KarnsTemporalSundering()));
            harness.addMana(player1, ManaColor.BLUE, 6);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            harness.castAndResolveSorcery(player1, 0, List.of(player1.getId()));

            GameData gd = harness.getGameData();
            assertThat(gd.extraTurns).hasSize(1);
            assertThat(gd.extraTurns).containsExactly(player1.getId());
        });
    }

    @Test
    @DisplayName("Extra turn is taken after current turn ends")
    void extraTurnIsTakenAfterCurrentTurnEnds() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.addToBattlefield(player1, new ArvadTheCursed());
            harness.setHand(player1, List.of(new KarnsTemporalSundering()));
            harness.addMana(player1, ManaColor.BLUE, 6);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            GameData gd = harness.getGameData();
            int turnBefore = gd.turnNumber;

            harness.castAndResolveSorcery(player1, 0, List.of(player1.getId()));

            advanceTurn();

            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
            assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
            assertThat(gd.extraTurns).isEmpty();
        });
    }

    @Test
    @DisplayName("Can target opponent for extra turn")
    void canTargetOpponentForExtraTurn() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.addToBattlefield(player1, new ArvadTheCursed());
            harness.setHand(player1, List.of(new KarnsTemporalSundering()));
            harness.addMana(player1, ManaColor.BLUE, 6);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            harness.castAndResolveSorcery(player1, 0, List.of(player2.getId()));

            GameData gd = harness.getGameData();
            assertThat(gd.extraTurns).containsExactly(player2.getId());
        });
    }

    @Test
    @DisplayName("Returns target nonland permanent to its owner's hand")
    void returnsNonlandPermanentToHand() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new BalothGorger());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID creatureId = harness.getPermanentId(player2, "Baloth Gorger");

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), creatureId));

        harness.assertNotOnBattlefield(player2, "Baloth Gorger");
        harness.assertInHand(player2, "Baloth Gorger");
    }

    @Test
    @DisplayName("Can be cast with only a player target (no permanent target)")
    void canCastWithOnlyPlayerTarget() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId()));

        GameData gd = harness.getGameData();
        // Should still resolve — extra turn granted, no bounce
        assertThat(gd.stack).isEmpty();
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Bounce target is removed before resolution — extra turn still resolves")
    void bounceTargetRemovedBeforeResolution() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.addToBattlefield(player1, new ArvadTheCursed());
            harness.addToBattlefield(player2, new BalothGorger());
            harness.setHand(player1, List.of(new KarnsTemporalSundering()));
            harness.addMana(player1, ManaColor.BLUE, 6);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            UUID creatureId = harness.getPermanentId(player2, "Baloth Gorger");

            harness.castSorcery(player1, 0, List.of(player1.getId(), creatureId));

            // Remove the creature before resolution
            harness.getGameData().playerBattlefields.get(player2.getId())
                    .removeIf(p -> p.getCard().getName().equals("Baloth Gorger"));

            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            // Extra turn still granted even though bounce target is gone
            assertThat(gd.extraTurns).containsExactly(player1.getId());
        });
    }

    @Test
    @DisplayName("Is exiled after resolution instead of going to graveyard")
    void isExiledAfterResolution() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new KarnsTemporalSundering()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Karn's Temporal Sundering");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Karn's Temporal Sundering"));
    }

    @Test
    @DisplayName("Extra turn + bounce + exile all resolve correctly together")
    void allEffectsResolveTogether() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.addToBattlefield(player1, new ArvadTheCursed());
            harness.addToBattlefield(player2, new BalothGorger());
            harness.setHand(player1, List.of(new KarnsTemporalSundering()));
            harness.addMana(player1, ManaColor.BLUE, 6);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            UUID creatureId = harness.getPermanentId(player2, "Baloth Gorger");

            harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), creatureId));

            GameData gd = harness.getGameData();

            // Extra turn granted
            assertThat(gd.extraTurns).containsExactly(player1.getId());

            // Creature bounced to hand
            harness.assertNotOnBattlefield(player2, "Baloth Gorger");
            harness.assertInHand(player2, "Baloth Gorger");

            // Spell exiled
            harness.assertNotInGraveyard(player1, "Karn's Temporal Sundering");
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Karn's Temporal Sundering"));
        });
    }
}
