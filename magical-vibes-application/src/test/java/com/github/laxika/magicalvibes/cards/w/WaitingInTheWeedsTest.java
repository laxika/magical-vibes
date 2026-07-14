package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WaitingInTheWeedsTest extends BaseCardTest {

    private long catCount(java.util.UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals("Cat"))
                .count();
    }

    @Test
    @DisplayName("Each player creates a Cat for each untapped Forest they control")
    void eachPlayerCreatesCatsForOwnUntappedForests() {
        // Player 1: three Forests, one tapped -> two untapped.
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent tappedForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        tappedForest.tap();

        // Player 2: one untapped Forest.
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new WaitingInTheWeeds()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(catCount(player1.getId())).isEqualTo(2);
        assertThat(catCount(player2.getId())).isEqualTo(1);

        Permanent cat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cat"))
                .findFirst()
                .orElseThrow();
        assertThat(cat.getCard().getPower()).isEqualTo(1);
        assertThat(cat.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A player with only tapped Forests creates no Cats")
    void noUntappedForestsCreatesNoCats() {
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new Forest());
        tapped.tap();

        harness.setHand(player1, List.of(new WaitingInTheWeeds()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(catCount(player1.getId())).isZero();
        assertThat(catCount(player2.getId())).isZero();
    }
}
