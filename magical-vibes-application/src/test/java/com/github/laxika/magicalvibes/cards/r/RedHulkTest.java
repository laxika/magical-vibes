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
        harness.assertLife(player1, 18);
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

    @Test
    @DisplayName("Enrage does not choose a damage target before the counter ability resolves")
    void initialEnrageTriggerDoesNotChooseTarget() {
        Permanent redHulk = harness.addToBattlefieldAndReturn(player2, new RedHulk());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, redHulk.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(redHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The counter is already placed when the reflexive damage target is chosen")
    void counterIsPlacedBeforeDamageTargetChoice() {
        Permanent redHulk = harness.addToBattlefieldAndReturn(player2, new RedHulk());
        shockRedHulk(redHulk);

        assertThat(redHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Losing the reflexive damage target does not undo the enrage counter")
    void counterRemainsWhenDamageTargetDiesInResponse() {
        Permanent redHulk = harness.addToBattlefieldAndReturn(player2, new RedHulk());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        shockRedHulk(redHulk);
        harness.handlePermanentChosen(player2, bears.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(redHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    private void shockRedHulk(Permanent redHulk) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, redHulk.getId());
        harness.passBothPriorities();
    }
}
