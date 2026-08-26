package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({AssimilateEssence.class, LlanowarElves.class})
class AssimilateEssenceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the creature spell when its controller cannot pay {4}")
    void countersWhenControllerCannotPay() {
        LlanowarElves elves = castTargetSpell(1);

        harness.setHand(player2, List.of(new AssimilateEssence()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> "Incubator".equals(permanent.getCard().getName()));
    }

    @Test
    @DisplayName("When its controller pays {4}, it creates an Incubator with two +1/+1 counters")
    void payingCreatesIncubatorForAssimilateEssenceController() {
        LlanowarElves elves = castTargetSpell(5);

        harness.setHand(player2, List.of(new AssimilateEssence()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent incubator = findPermanent(player2, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Counters the spell and does not incubate when its controller declines to pay")
    void decliningDoesNotIncubate() {
        LlanowarElves elves = castTargetSpell(5);

        harness.setHand(player2, List.of(new AssimilateEssence()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> "Incubator".equals(permanent.getCard().getName()));
    }

    private LlanowarElves castTargetSpell(int manaAvailable) {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, manaAvailable);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        return elves;
    }
}
