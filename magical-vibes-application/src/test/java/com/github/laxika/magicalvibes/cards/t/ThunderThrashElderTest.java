package com.github.laxika.magicalvibes.cards.t;

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

class ThunderThrashElderTest extends BaseCardTest {

    private void castElder() {
        harness.setHand(player1, new ArrayList<>(List.of(new ThunderThrashElder())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
    }

    private Permanent elder() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Thunder-Thrash Elder"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Devouring two creatures gives six +1/+1 counters (Devour 3)")
    void devourTwoAddsSixCounters() {
        Permanent fodder1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fodder2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castElder();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder1.getId(), fodder2.getId()));

        assertThat(elder().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Devouring nothing enters with no counters")
    void devourNoneNoCounters() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castElder();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(elder().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("With no other creatures, enters with no counters and no prompt")
    void noOtherCreaturesNoPrompt() {
        castElder();
        harness.passBothPriorities(); // resolve creature spell (no devour prompt)

        assertThat(elder().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
