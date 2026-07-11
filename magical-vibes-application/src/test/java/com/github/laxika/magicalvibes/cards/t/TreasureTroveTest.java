package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TreasureTroveTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability with mana draws a card")
    void activatingDrawsACard() {
        Permanent trove = addTrove(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, indexOf(player1, trove), null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()).get(1).getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Ability can be activated repeatedly since it does not tap")
    void canActivateRepeatedly() {
        Permanent trove = addTrove(player1);
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.activateAbility(player1, indexOf(player1, trove), null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, trove), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        Permanent trove = addTrove(player1);
        setDeck(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, trove), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addTrove(Player player) {
        Permanent perm = new Permanent(new TreasureTrove());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }

    private void setDeck(Player player, List<? extends com.github.laxika.magicalvibes.model.Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
