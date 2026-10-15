package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrivenDespair.class, FrilledSandwalla.class, Forest.class})
class DrivenDespairTest extends BaseCardTest {

    @Test
    @DisplayName("Driven grants trample and combat-damage draw to creatures you control")
    void drivenGrantsTrampleAndDrawOnCombatDamage() {
        Permanent bears = addCreatureReady(player1, new FrilledSandwalla());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new FrilledSandwalla());

        harness.setHand(player1, List.of(new DrivenDespair()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opponent.hasKeyword(Keyword.TRAMPLE)).isFalse();

        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        bears.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertInGraveyard(player1, "Driven");
    }

    @Test
    @DisplayName("Driven effects wear off at end of turn")
    void drivenEffectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new FrilledSandwalla());
        harness.setHand(player1, List.of(new DrivenDespair()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(bears.getTemporaryTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).isNotEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(bears.getTemporaryTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).isEmpty();
    }

    @Test
    @DisplayName("Driven does not grant abilities to creatures that enter after it resolves")
    void drivenDoesNotAffectLaterEntrants() {
        Permanent early = harness.addToBattlefieldAndReturn(player1, new FrilledSandwalla());
        harness.setHand(player1, List.of(new DrivenDespair()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent late = harness.addToBattlefieldAndReturn(player1, new FrilledSandwalla());

        assertThat(early.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(late.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(late.getTemporaryTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).isEmpty();
    }

    @Test
    @DisplayName("Despair from graveyard grants menace and combat-damage discard, then exiles")
    void despairFlashbackGrantsMenaceAndDiscardThenExiles() {
        Permanent bears = addCreatureReady(player1, new FrilledSandwalla());
        harness.setGraveyard(player1, List.of(new DrivenDespair()));
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(bears.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Driven") || c.getName().equals("Despair"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Driven"));

        bears.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Despair menace and granted trigger wear off at end of turn")
    void despairEffectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new FrilledSandwalla());
        harness.setGraveyard(player1, List.of(new DrivenDespair()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.MENACE)).isFalse();
        assertThat(bears.getTemporaryTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).isEmpty();
    }

    @Test
    @DisplayName("Despair requires sorcery timing")
    void despairRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new DrivenDespair()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }

    @Test
    @DisplayName("Repeated Driven casts grant separate draw triggers")
    void repeatedDrivenCastsDrawTwice() {
        Permanent creature = addCreatureReady(player1, new FrilledSandwalla());
        harness.setHand(player1, List.of(new DrivenDespair(), new DrivenDespair()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);

        creature.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Despair does not affect opposing creatures or later entrants")
    void despairOnlyAffectsOwnCreaturesPresentOnResolution() {
        Permanent early = addCreatureReady(player1, new FrilledSandwalla());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new FrilledSandwalla());
        harness.setGraveyard(player1, List.of(new DrivenDespair()));
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveFlashback(player1, 0, null);
        Permanent late = addCreatureReady(player1, new FrilledSandwalla());

        assertThat(early.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(opponent.hasKeyword(Keyword.MENACE)).isFalse();
        assertThat(late.hasKeyword(Keyword.MENACE)).isFalse();

        late.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Driven and Despair together grant both combat damage abilities")
    void bothHalvesGrantDrawAndDiscard() {
        Permanent creature = addCreatureReady(player1, new FrilledSandwalla());
        harness.setHand(player1, List.of(new DrivenDespair()));
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(creature.hasKeyword(Keyword.MENACE)).isTrue();
        creature.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }
}
