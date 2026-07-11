package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlindingLightTest extends BaseCardTest {

    private void castBlindingLight(int cardIndex) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, cardIndex, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Taps nonwhite creatures on both sides")
    void tapsNonwhiteCreatures() {
        Permanent p1Bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent p2Bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlindingLight()));

        castBlindingLight(0);

        assertThat(p1Bears.isTapped()).isTrue();
        assertThat(p2Bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap white creatures")
    void doesNotTapWhiteCreatures() {
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlindingLight()));

        castBlindingLight(0);

        assertThat(whiteCreature.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Works with empty battlefield and resolves to graveyard")
    void worksWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new BlindingLight()));

        castBlindingLight(0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blinding Light"));
    }
}
