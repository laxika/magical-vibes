package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllosaurusRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness are one plus the number of lands its controller controls")
    void ptEqualsOnePlusControlledLands() {
        Permanent rider = addCreatureReady(player1, new AllosaurusRider());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(3);

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exiling two green cards from hand pays the alternative cost")
    void alternativeCostExilesTwoGreenCards() {
        harness.setHand(player1, List.of(new AllosaurusRider(), new GrizzlyBears(), new GrizzlyBears()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, 1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AllosaurusRider);
    }

    @Test
    @DisplayName("The alternative cost requires two green cards")
    void alternativeCostRejectsNonGreenCard() {
        harness.setHand(player1, List.of(new AllosaurusRider(), new Forest(), new Plains()));

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, 1, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }
}
