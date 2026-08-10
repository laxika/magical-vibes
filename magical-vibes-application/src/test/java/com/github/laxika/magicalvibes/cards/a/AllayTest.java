package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllayTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Allay destroys target enchantment")
    void resolvingDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Allay");
    }

    @Test
    @DisplayName("Paying buyback returns Allay to its owner's hand")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castInstantWithBuyback(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(playerHandNames(player1)).containsExactly("Allay");
        assertThat(graveyardNames(player1)).doesNotContain("Allay");
    }

    @Test
    @DisplayName("Allay cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<String> playerHandNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(card -> card.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(card -> card.getName()).toList();
    }
}
