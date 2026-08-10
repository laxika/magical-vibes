package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ForbidTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell without buyback")
    void countersSpellWithoutBuyback() {
        GrizzlyBears bears = castTargetSpell();
        harness.setHand(player2, List.of(new Forbid()));
        addForbidMana();

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Forbid");
    }

    @Test
    @DisplayName("Discarding two cards for buyback returns Forbid to its owner's hand")
    void discardBuybackReturnsToHand() {
        GrizzlyBears bears = castTargetSpell();
        harness.setHand(player2, List.of(new Forbid(), new Shock(), new MightOfOaks()));
        addForbidMana();

        harness.castInstantWithDiscardBuyback(player2, 0, bears.getId(), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(handNames(player2)).containsExactly("Forbid");
        assertThat(graveyardNames(player2)).containsExactlyInAnyOrder("Shock", "Might of Oaks");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Buyback requires exactly two other cards to be discarded")
    void buybackRequiresTwoCards() {
        castTargetSpell();
        harness.setHand(player2, List.of(new Forbid(), new Shock()));
        addForbidMana();

        assertThatThrownBy(() -> harness.castInstantWithDiscardBuyback(player2, 0, gd.stack.getFirst().getCard().getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(handNames(player2)).containsExactly("Forbid", "Shock");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("A fizzled buyback Forbid goes to the graveyard")
    void fizzleDoesNotReturnBuybackSpell() {
        GrizzlyBears bears = castTargetSpell();
        harness.setHand(player2, List.of(new Forbid(), new Shock(), new MightOfOaks()));
        addForbidMana();

        harness.castInstantWithDiscardBuyback(player2, 0, bears.getId(), List.of(1, 2));
        gd.stack.removeIf(entry -> entry.getCard().getId().equals(bears.getId()));
        harness.passBothPriorities();

        assertThat(handNames(player2)).isEmpty();
        assertThat(graveyardNames(player2)).contains("Forbid", "Shock", "Might of Oaks");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Forbid can target only a spell")
    void cannotTargetPlayer() {
        harness.setHand(player2, List.of(new Forbid()));
        addForbidMana();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private GrizzlyBears castTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        return bears;
    }

    private void addForbidMana() {
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }
}
