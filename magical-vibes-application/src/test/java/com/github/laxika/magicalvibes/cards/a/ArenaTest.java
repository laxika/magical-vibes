package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Arena.class, GrizzlyBears.class, LlanowarElves.class})
class ArenaTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent chooses the second creature, which is tapped and fights the first")
    void opponentChoosesSecondCreatureAndItFights() {
        Permanent arena = harness.addToBattlefieldAndReturn(player1, new Arena());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, bears.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).containsExactly(elves.getId());

        harness.handlePermanentChosen(player2, elves.getId());
        harness.passBothPriorities();

        assertThat(arena.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Taps the remaining target but does not fight when one target is gone")
    void tapsRemainingTargetWhenOpponentTargetIsGone() {
        Permanent arena = harness.addToBattlefieldAndReturn(player1, new Arena());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.handlePermanentChosen(player2, elves.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(arena.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getMarkedDamage()).isZero();
    }
}
