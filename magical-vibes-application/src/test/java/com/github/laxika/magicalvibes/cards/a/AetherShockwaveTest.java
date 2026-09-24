package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FiddleheadKami;
import com.github.laxika.magicalvibes.cards.i.InnerChamberGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherShockwave.class, FiddleheadKami.class, InnerChamberGuard.class})
class AetherShockwaveTest extends BaseCardTest {

    @Test
    @DisplayName("First mode taps all Spirits and leaves non-Spirit creatures untapped")
    void tapsAllSpirits() {
        harness.addToBattlefield(player1, new FiddleheadKami());
        harness.addToBattlefield(player1, new InnerChamberGuard());
        harness.addToBattlefield(player2, new FiddleheadKami());
        harness.addToBattlefield(player2, new InnerChamberGuard());
        harness.setHand(player1, List.of(new AetherShockwave()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fiddlehead Kami").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Fiddlehead Kami").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Inner-Chamber Guard").isTapped()).isFalse();
        assertThat(findPermanent(player2, "Inner-Chamber Guard").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Second mode taps all non-Spirit creatures and leaves Spirits untapped")
    void tapsAllNonSpiritCreatures() {
        harness.addToBattlefield(player1, new FiddleheadKami());
        harness.addToBattlefield(player1, new InnerChamberGuard());
        harness.addToBattlefield(player2, new FiddleheadKami());
        harness.addToBattlefield(player2, new InnerChamberGuard());
        harness.setHand(player1, List.of(new AetherShockwave()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fiddlehead Kami").isTapped()).isFalse();
        assertThat(findPermanent(player2, "Fiddlehead Kami").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Inner-Chamber Guard").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Inner-Chamber Guard").isTapped()).isTrue();
    }
}
