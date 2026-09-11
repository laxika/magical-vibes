package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeSayTheeNay.class, GrizzlyBears.class, LlanowarElves.class})
class WeSayTheeNayTest extends BaseCardTest {

    @Test
    void usesTwoWhenTeamworkIsNotPaid() {
        LlanowarElves elves = castTargetSpellWithTwoManaRemaining();

        castWeSayTheeNay(elves, List.of());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void usesFourWhenTeamworkIsPaid() {
        Permanent teammate = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        LlanowarElves elves = castTargetSpellWithThreeManaRemaining();

        castWeSayTheeNay(elves, List.of(teammate.getId()));

        assertThat(teammate.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private LlanowarElves castTargetSpellWithTwoManaRemaining() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        return elves;
    }

    private LlanowarElves castTargetSpellWithThreeManaRemaining() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        return elves;
    }

    private void castWeSayTheeNay(LlanowarElves elves, List<java.util.UUID> teamworkIds) {
        harness.setHand(player2, List.of(new WeSayTheeNay()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstantWithSacrifices(player2, 0, elves.getId(), teamworkIds);
        harness.passBothPriorities();
    }
}
