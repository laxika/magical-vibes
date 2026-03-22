package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZhalfirinVoidTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ETB scry 1 effect")
    void hasEtbScryEffect() {
        ZhalfirinVoid card = new ZhalfirinVoid();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ScryEffect.class);
        ScryEffect effect = (ScryEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Has tap for colorless mana ability")
    void hasTapForColorlessManaAbility() {
        ZhalfirinVoid card = new ZhalfirinVoid();

        assertThat(card.getEffects(EffectSlot.ON_TAP)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_TAP).getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.COLORLESS));
    }

    // ===== ETB trigger =====

    @Test
    @DisplayName("Playing Zhalfirin Void puts ETB trigger on the stack")
    void playingPutsEtbTriggerOnStack() {
        playZhalfirinVoid(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Zhalfirin Void");
    }

    @Test
    @DisplayName("Resolving ETB enters scry state with 1 card")
    void resolvingEtbEntersScryState() {
        playZhalfirinVoid(player1);
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);
        assertThat(gd.interaction.scryContext()).isNotNull();
        assertThat(gd.interaction.scryContext().cards()).hasSize(1);
    }

    // ===== Scry 1 functionality =====

    @Test
    @DisplayName("Scry 1 keeping card on top preserves it")
    void scryKeepOnTop() {
        playZhalfirinVoid(player1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.get(0);

        harness.passBothPriorities(); // resolve ETB

        harness.getGameService().handleScryCompleted(gd, player1, List.of(0), List.of());

        assertThat(deck.get(0)).isSameAs(originalTop);
    }

    @Test
    @DisplayName("Scry 1 putting card on bottom moves it to bottom")
    void scryPutOnBottom() {
        playZhalfirinVoid(player1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.get(0);

        harness.passBothPriorities(); // resolve ETB

        harness.getGameService().handleScryCompleted(gd, player1, List.of(), List.of(0));

        assertThat(deck.get(0)).isNotSameAs(originalTop);
        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop);
    }

    @Test
    @DisplayName("Completing scry clears awaiting state")
    void scryCompletionClearsState() {
        playZhalfirinVoid(player1);
        harness.passBothPriorities(); // resolve ETB

        harness.getGameService().handleScryCompleted(gd, player1, List.of(0), List.of());

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.interaction.scryContext()).isNull();
    }

    // ===== Land enters battlefield =====

    @Test
    @DisplayName("Zhalfirin Void enters the battlefield as a permanent")
    void entersBattlefieldAsPermanent() {
        playZhalfirinVoid(player1);
        harness.passBothPriorities(); // resolve ETB
        harness.getGameService().handleScryCompleted(gd, player1, List.of(0), List.of());

        harness.assertOnBattlefield(player1, "Zhalfirin Void");
    }

    @Test
    @DisplayName("Stack is empty after ETB and scry fully resolve")
    void stackEmptyAfterResolution() {
        playZhalfirinVoid(player1);
        harness.passBothPriorities(); // resolve ETB
        harness.getGameService().handleScryCompleted(gd, player1, List.of(0), List.of());

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private void playZhalfirinVoid(Player player) {
        harness.setHand(player, List.of(new ZhalfirinVoid()));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player, 0);
    }
}
