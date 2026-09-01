package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SageOfMysteries.class, GloriousAnthem.class, Forest.class})
class SageOfMysteriesTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control makes a target player mill two cards")
    void ownEnchantmentEntryMillsTargetPlayer() {
        harness.addToBattlefield(player1, new SageOfMysteries());
        setLibrary(player2, 3);
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new SageOfMysteries());
        setLibrary(player2, 3);
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void setLibrary(com.github.laxika.magicalvibes.model.Player player, int size) {
        List<Card> library = List.of(new Forest(), new Forest(), new Forest());
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(library.subList(0, size));
    }
}
