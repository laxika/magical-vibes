package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WiltLeafLiegeTest extends BaseCardTest {

    // ===== Static +1/+1 boosts =====

    @Test
    @DisplayName("Other green creatures you control get +1/+1")
    void buffsOwnGreenCreatures() {
        harness.addToBattlefield(player1, new WiltLeafLiege());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Other white creatures you control get +1/+1")
    void buffsOwnWhiteCreatures() {
        harness.addToBattlefield(player1, new WiltLeafLiege());
        Permanent vanguard = addCreatureReady(player1, new EliteVanguard());

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures that are neither green nor white are not buffed")
    void doesNotBuffOtherColors() {
        harness.addToBattlefield(player1, new WiltLeafLiege());
        Permanent giant = addCreatureReady(player1, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's green creatures are not buffed")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new WiltLeafLiege());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        Permanent liege = harness.addToBattlefieldAndReturn(player1, new WiltLeafLiege());

        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(4);
    }

    @Test
    @DisplayName("A green-white creature gets both bonuses (+2/+2)")
    void greenWhiteCreatureGetsBothBonuses() {
        harness.addToBattlefield(player1, new WiltLeafLiege());
        // A second Liege is both green and white, so it gets +1/+1 twice from the first.
        Permanent secondLiege = addCreatureReady(player1, new WiltLeafLiege());

        assertThat(gqs.getEffectivePower(gd, secondLiege)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, secondLiege)).isEqualTo(6);
    }

    // ===== Discard-to-battlefield replacement =====

    @Test
    @DisplayName("Enters battlefield when discarded by opponent via Distress")
    void entersBattlefieldWhenDiscardedByOpponent() {
        harness.setHand(player2, new ArrayList<>(List.of(new WiltLeafLiege())));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player1 chooses Wilt-Leaf Liege from player2's revealed hand
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wilt-Leaf Liege"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Wilt-Leaf Liege"));
    }
}
