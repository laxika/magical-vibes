package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.r.ResoundingWave;
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

@CardUsed({Skullmulcher.class, CylianElf.class, ResoundingWave.class})
class SkullmulcherTest extends BaseCardTest {

    private void castSkullmulcher() {
        harness.castFromHand(player1, new Skullmulcher(), "{4}{G}");
    }

    private Permanent skullmulcher() {
        return findPermanent(player1, "Skullmulcher");
    }

    @Test
    @DisplayName("Devouring two creatures enters with two counters and draws two cards")
    void devourTwoAddsCountersAndDrawsTwo() {
        Permanent fodderA = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent fodderB = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        castSkullmulcher();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodderA.getId(), fodderB.getId()));

        Permanent skullmulcher = skullmulcher();
        assertThat(skullmulcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities(); // resolve draw trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Devouring nothing enters with no counters and draws nothing")
    void devourNoneNoCountersNoDraw() {
        harness.addToBattlefieldAndReturn(player1, new CylianElf());
        castSkullmulcher();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        Permanent skullmulcher = skullmulcher();
        assertThat(skullmulcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities(); // resolve draw trigger (draws 0)

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With no creatures to devour, entry needs no choice and draws nothing")
    void noCreaturesNeedsNoChoice() {
        harness.addToBattlefield(player2, new CylianElf());
        castSkullmulcher();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(skullmulcher().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Cylian Elf");
    }

    @Test
    @DisplayName("Devour can sacrifice only a subset and the draw waits for its trigger")
    void devourSubsetLeavesOtherCreatures() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        castSkullmulcher();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(skullmulcher().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(unchosen).doesNotContain(chosen);
        harness.assertInGraveyard(player1, "Cylian Elf");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The draw uses the devoured count even after Skullmulcher leaves")
    void drawAfterSourceLeavesBattlefield() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setLibrary(player1, List.of(new CylianElf()));
        castSkullmulcher();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, skullmulcher().getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Skullmulcher");
        harness.assertInHand(player1, "Skullmulcher");

        harness.passBothPriorities();
        harness.assertInHand(player1, "Cylian Elf");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
