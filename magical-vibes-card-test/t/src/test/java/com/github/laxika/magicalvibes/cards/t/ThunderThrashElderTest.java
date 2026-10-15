package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.j.Jund;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderThrashElder.class, CylianElf.class})
class ThunderThrashElderTest extends BaseCardTest {

    private void castElder() {
        harness.castFromHand(player1, new ThunderThrashElder(), "{2}{R}");
    }

    private Permanent elder() {
        return findPermanent(player1, "Thunder-Thrash Elder");
    }

    @Test
    @DisplayName("Devouring two creatures gives six +1/+1 counters (Devour 3)")
    void devourTwoAddsSixCounters() {
        Permanent fodder1 = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent fodder2 = harness.addToBattlefieldAndReturn(player1, new CylianElf());

        castElder();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder1.getId(), fodder2.getId()));

        assertThat(elder().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Devouring nothing enters with no counters")
    void devourNoneNoCounters() {
        harness.addToBattlefield(player1, new CylianElf());

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

    @Test
    @DisplayName("Devour may sacrifice just one of multiple creatures")
    void devourOnlyOneCreature() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent kept = harness.addToBattlefieldAndReturn(player1, new CylianElf());

        castElder();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(elder().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kept).doesNotContain(chosen);
        harness.assertInGraveyard(player1, "Cylian Elf");
    }

    @Test
    @CardUsed({Mountain.class})
    @DisplayName("Devour cannot sacrifice an opponent's creature or a noncreature")
    void rejectsIneligiblePermanents() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());

        castElder();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(opponent.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        assertThat(elder().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Cylian Elf");
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @CardUsed({Jund.class})
    @DisplayName("Printed devour and devour granted by Jund each allow separate sacrifices")
    void printedAndGrantedDevourBothApply() {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Jund(), gd.nextTimestamp()));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CylianElf());

        castElder();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(elder().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        harness.assertNotOnBattlefield(player1, "Cylian Elf");
    }
}
