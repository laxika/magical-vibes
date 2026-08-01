package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SearchWarrantTest extends BaseCardTest {

    private void castSearchWarrant(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new SearchWarrant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Controller gains life equal to target player's hand size")
    void gainsLifeEqualToTargetHandSize() {
        harness.setLife(player1, 20);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Island(), new GrizzlyBears())));

        castSearchWarrant(player2.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Search Warrant");
    }

    @Test
    @DisplayName("Uses target hand size on resolution")
    void usesHandSizeOnResolution() {
        harness.setLife(player1, 20);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Island())));

        harness.setHand(player1, List.of(new SearchWarrant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, player2.getId());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Gains 0 life if target hand is empty")
    void emptyHandGainsZero() {
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of());

        castSearchWarrant(player2.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        // Search Warrant itself is in hand when cast, so set other cards after cast is not needed —
        // cast empties the hand of the spell; seed extra cards via a second set after life setup.
        harness.setHand(player1, new ArrayList<>(List.of(new SearchWarrant(), new Forest(), new Island())));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // After casting, hand still has Forest + Island (2 cards).
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.setHand(player1, List.of(new SearchWarrant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
