package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CateranBrute;
import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectersWail.class, CateranBrute.class, DeadlyInsect.class})
class SpectersWailTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Specter's Wail targeting any player puts it on the stack")
    void castingTargetsAnyPlayer() {
        harness.setHand(player1, List.of(new SpectersWail()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Resolving makes the target player discard a card at random")
    void discardsOneAtRandom() {
        CateranBrute firstCard = new CateranBrute();
        DeadlyInsect secondCard = new DeadlyInsect();
        harness.setHand(player2, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new SpectersWail()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsAnyOf(firstCard, secondCard);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent target = addCreatureReady(player2, new DeadlyInsect());
        harness.setHand(player1, List.of(new SpectersWail()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This spell can only target players");
    }

    @Test
    @DisplayName("A player with one card discards that card without choosing")
    void discardsOnlyCard() {
        CateranBrute discarded = new CateranBrute();
        DeadlyInsect retained = new DeadlyInsect();
        SpectersWail spell = new SpectersWail();
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(spell, retained));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Self-targeting discards from the caster's remaining hand")
    void selfTargetDiscardsRemainingCard() {
        SpectersWail spell = new SpectersWail();
        CateranBrute discarded = new CateranBrute();
        DeadlyInsect opponentCard = new DeadlyInsect();
        harness.setHand(player1, List.of(spell, discarded));
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(spell, discarded);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Self-targeting with only the spell in hand discards nothing")
    void selfTargetWithNoRemainingCards() {
        SpectersWail spell = new SpectersWail();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Targeting a player with an empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new SpectersWail()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
