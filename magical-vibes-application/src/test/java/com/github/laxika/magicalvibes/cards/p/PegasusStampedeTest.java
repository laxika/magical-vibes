package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PegasusStampedeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 white Pegasus token with flying")
    void createsPegasusToken() {
        Permanent land = addLand(player1);
        cast(false, land);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent token = findPermanentByName(player1, "Pegasus");
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(graveyardNames(player1)).containsExactly("Pegasus Stampede");
    }

    @Test
    @DisplayName("Paying buyback sacrifices a land and returns Pegasus Stampede to hand")
    void buybackSacrificesLandAndReturnsToHand() {
        Permanent land = addLand(player1);
        cast(true, land);

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(land::equals);
        assertThat(findPermanentByName(player1, "Pegasus")).satisfies(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
        });
        assertThat(handNames(player1)).containsExactly("Pegasus Stampede");
        assertThat(graveyardNames(player1)).doesNotContain("Pegasus Stampede");
    }

    @Test
    @DisplayName("Buyback cannot be paid without sacrificing a land")
    void buybackRequiresLand() {
        harness.setHand(player1, List.of(new PegasusStampede()));
        addManaForSpell(player1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrificeAndBuyback(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private void cast(boolean buyback, Permanent land) {
        harness.setHand(player1, List.of(new PegasusStampede()));
        addManaForSpell(player1);
        if (buyback) {
            harness.castSorceryWithSacrificeAndBuyback(player1, 0, land.getId());
        } else {
            harness.castSorcery(player1, 0, 0);
        }
        harness.passBothPriorities();
    }

    private void addManaForSpell(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private Permanent findPermanentByName(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(card -> card.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(card -> card.getName()).toList();
    }
}
