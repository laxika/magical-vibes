package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyeOfDoom.class, Forest.class, GrizzlyBears.class})
class EyeOfDoomTest extends BaseCardTest {

    @Test
    void eachPlayerChoosesOwnNonlandPermanentForDoomCounter() {
        Permanent player1Chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1Other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        Permanent player2Chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2Other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new EyeOfDoom()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent eye = findPermanent(player1, "Eye of Doom");
        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactlyInAnyOrder(
                eye.getId(), player1Chosen.getId(), player1Other.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(player1Chosen.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactly(player2Chosen.getId(), player2Other.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(player2Chosen.getId()));

        assertThat(player1Chosen.getCounterCount(CounterType.DOOM)).isEqualTo(1);
        assertThat(player2Chosen.getCounterCount(CounterType.DOOM)).isEqualTo(1);
        assertThat(player1Other.getCounterCount(CounterType.DOOM)).isZero();
        assertThat(player2Other.getCounterCount(CounterType.DOOM)).isZero();
    }

    @Test
    void sacrificeAbilityDestroysEveryPermanentWithDoomCounter() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new EyeOfDoom());
        eye.setSummoningSick(false);
        Permanent markedOwn = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent markedOpponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent unmarked = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        markedOwn.setCounterCount(CounterType.DOOM, 1);
        markedOpponent.setCounterCount(CounterType.DOOM, 1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Eye of Doom");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unmarked);
    }
}
