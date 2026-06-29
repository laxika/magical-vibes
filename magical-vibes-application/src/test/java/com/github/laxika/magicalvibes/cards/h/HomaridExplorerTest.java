package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HomaridExplorerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Homarid Explorer has correct card properties")
    void hasCorrectProperties() {
        HomaridExplorer card = new HomaridExplorer();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MillTargetPlayerEffect.class);
        MillTargetPlayerEffect effect =
                (MillTargetPlayerEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(4);
    }

    // ===== Resolving creature spell =====

    @Test
    @DisplayName("Resolving puts Homarid Explorer on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castHomaridExplorer(player2.getId());
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Homarid Explorer"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    // ===== ETB mill =====

    @Test
    @DisplayName("ETB trigger mills four cards from target player's library")
    void etbMillsFourCards() {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        castHomaridExplorer(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Can target yourself to mill your own library")
    void canTargetSelf() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        castHomaridExplorer(player1.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Mills only remaining cards when library has fewer than four")
    void millsOnlyRemainingWhenLibrarySmall() {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 2) {
            deck.removeFirst();
        }

        castHomaridExplorer(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mills nothing when library is empty")
    void millsNothingWhenLibraryEmpty() {
        gd.playerDecks.get(player2.getId()).clear();

        castHomaridExplorer(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castHomaridExplorer(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private void castHomaridExplorer(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new HomaridExplorer()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, targetPlayerId, null);
    }
}
