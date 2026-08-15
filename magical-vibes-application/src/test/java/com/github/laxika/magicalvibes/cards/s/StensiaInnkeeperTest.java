package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StensiaInnkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target land an opponent controls and keeps it tapped through its next untap step")
    void tapsAndLocksOpponentsLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new StensiaInnkeeper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, forestId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent forest = findPermanent(player2, "Forest");
        assertThat(forest.isTapped()).isTrue();
        assertThat(forest.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a land you control")
    void cannotTargetOwnLand() {
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.setHand(player1, List.of(new StensiaInnkeeper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, forestId, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
