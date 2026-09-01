package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonsJester.class, GrizzlyBears.class})
class DemonsJesterTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+1 with an empty hand")
    void getsHellbentBonusWithEmptyHand() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new DemonsJester());

        Permanent jester = findPermanent(player1, "Demon's Jester");
        assertThat(gqs.getEffectivePower(gd, jester)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jester)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses the bonus while its controller has a card in hand")
    void losesHellbentBonusWithCardInHand() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new DemonsJester());

        Permanent jester = findPermanent(player1, "Demon's Jester");
        assertThat(gqs.getEffectivePower(gd, jester)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jester)).isEqualTo(2);
    }

    @Test
    @DisplayName("The bonus changes dynamically when the hand changes")
    void bonusChangesWhenHandChanges() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new DemonsJester());

        Permanent jester = findPermanent(player1, "Demon's Jester");
        assertThat(gqs.getEffectivePower(gd, jester)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jester)).isEqualTo(3);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, jester)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jester)).isEqualTo(2);
    }
}
