package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbominableTreefolk.class, GrizzlyBears.class, SnowCoveredPlains.class})
class AbominableTreefolkTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of snow permanents you control")
    void ptEqualsControlledSnowPermanents() {
        Permanent treefolk = addCreatureReady(player1, new AbominableTreefolk());
        harness.addToBattlefield(player1, new SnowCoveredPlains());
        harness.addToBattlefield(player1, new SnowCoveredPlains());
        harness.addToBattlefield(player2, new SnowCoveredPlains());

        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB taps an opponent's creature and skips its next untap step")
    void etbTapsAndLocksOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTreefolkTargeting(player2, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AbominableTreefolk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTreefolkTargeting(Player targetOwner, UUID targetId) {
        harness.setHand(player1, List.of(new AbominableTreefolk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
