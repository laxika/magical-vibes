package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrivenDespairTest extends BaseCardTest {

    @Test
    @DisplayName("Driven grants trample and combat-damage draw to creatures you control")
    void drivenGrantsTrampleAndDrawOnCombatDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DrivenDespair()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opponent.hasKeyword(Keyword.TRAMPLE)).isFalse();

        setDeck(player1, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        bears.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Driven"));
    }

    @Test
    @DisplayName("Driven effects wear off at end of turn")
    void drivenEffectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DrivenDespair()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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
        Permanent early = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DrivenDespair()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent late = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(early.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(late.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(late.getTemporaryTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).isEmpty();
    }

    @Test
    @DisplayName("Despair from graveyard grants menace and combat-damage discard, then exiles")
    void despairFlashbackGrantsMenaceAndDiscardThenExiles() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new DrivenDespair()));
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Driven") || c.getName().equals("Despair"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Driven"));

        bears.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new DrivenDespair()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

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

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
