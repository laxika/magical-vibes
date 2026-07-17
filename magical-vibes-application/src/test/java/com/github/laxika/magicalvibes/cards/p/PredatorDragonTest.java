package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PredatorDragonTest extends BaseCardTest {

    private void castDragon() {
        harness.setHand(player1, new ArrayList<>(List.of(new PredatorDragon())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
    }

    private Permanent dragon() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Predator Dragon"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Devouring two creatures gives four +1/+1 counters (Devour 2)")
    void devourTwoAddsFourCounters() {
        Permanent fodder1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fodder2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDragon();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder1.getId(), fodder2.getId()));

        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Devouring nothing enters with no counters")
    void devourNoneNoCounters() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDragon();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("With no other creatures, enters with no counters and no prompt")
    void noOtherCreaturesNoPrompt() {
        castDragon();
        harness.passBothPriorities(); // resolve creature spell (no devour prompt)

        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
