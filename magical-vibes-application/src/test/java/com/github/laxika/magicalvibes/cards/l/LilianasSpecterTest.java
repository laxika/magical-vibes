package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LilianasSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Has ETB effect that makes each opponent discard one card")
    void hasEtbDiscardEffect() {
        LilianasSpecter card = new LilianasSpecter();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EachOpponentDiscardsEffect.class);
        EachOpponentDiscardsEffect effect =
                (EachOpponentDiscardsEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not require a target")
    void doesNotRequireTarget() {
        LilianasSpecter card = new LilianasSpecter();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingPutsEtbOnStack() {
        castLilianasSpecter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Liliana's Specter"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB trigger makes opponent discard one card")
    void etbMakesOpponentDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castLilianasSpecter();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.revealedHandChoice().discardRemainingCount()).isEqualTo(1);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB trigger does nothing when opponent has no cards in hand")
    void etbDoesNothingWithEmptyOpponentHand() {
        harness.setHand(player2, new ArrayList<>());
        castLilianasSpecter();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to discard"));
    }

    private void castLilianasSpecter() {
        harness.setHand(player1, List.of(new LilianasSpecter()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
    }
}
