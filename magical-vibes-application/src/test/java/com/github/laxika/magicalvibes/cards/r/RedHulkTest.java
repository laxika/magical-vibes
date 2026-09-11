package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedHulk.class, Shock.class, GrizzlyBears.class})
class RedHulkTest extends BaseCardTest {

    @Test
    @DisplayName("When dealt damage, Red Hulk gets a counter and deals damage equal to its counters")
    void enrageAddsCounterAndDealsScaledDamageToCreature() {
        Permanent redHulk = harness.addToBattlefieldAndReturn(player2, new RedHulk());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        shockRedHulk(redHulk);

        harness.handlePermanentChosen(player2, bears.getId());
        harness.passBothPriorities();

        assertThat(redHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Red Hulk's damage uses the counter count after adding the new counter")
    void enrageUsesUpdatedCounterCount() {
        Permanent redHulk = harness.addToBattlefieldAndReturn(player2, new RedHulk());
        redHulk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        shockRedHulk(redHulk);

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(redHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Red Hulk cannot target itself with its enrage trigger")
    void enrageDoesNotOfferItselfAsTarget() {
        Permanent redHulk = harness.addToBattlefieldAndReturn(player2, new RedHulk());
        shockRedHulk(redHulk);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).doesNotContain(redHulk.getId());
    }

    private void shockRedHulk(Permanent redHulk) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, redHulk.getId());
        harness.passBothPriorities();
    }
}
