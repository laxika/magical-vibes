package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IlluminatedFolioTest extends BaseCardTest {

    @Test
    @DisplayName("Activating with two color-sharing cards puts the ability on the stack and taps the Folio")
    void activatingWithSharingPairTapsAndStacks() {
        Permanent folio = addReadyFolio(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        // Two green cards share a color.
        harness.setHand(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(folio.isTapped()).isTrue();
        // Revealed cards stay in hand.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("reveals") && log.contains("as a cost"));
    }

    @Test
    @DisplayName("Resolving the ability draws a card")
    void resolvingDrawsACard() {
        addReadyFolio(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(3);
        assertThat(hand.get(2).getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Cannot activate without two cards that share a color")
    void cannotActivateWithoutSharingPair() {
        addReadyFolio(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        // Green + red + colorless: no two share a color.
        harness.setHand(player1, List.of(new GrizzlyBears(), new HillGiant(), new Ornithopter()));
        setDeck(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a color");
    }

    private Permanent addReadyFolio(Player player) {
        Permanent perm = new Permanent(new IlluminatedFolio());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setDeck(Player player, List<? extends Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
