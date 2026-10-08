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

    @Test
    void canPayFourToSaveSpellWhenTeamworkIsPaid() {
        Permanent teammate = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        LlanowarElves elves = castTargetSpellWithThreeManaRemaining();
        harness.addMana(player1, ManaColor.GREEN, 1);

        castWeSayTheeNay(elves, List.of(teammate.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        assertThat(teammate.isTapped()).isTrue();
    }

    @Test
    void canDeclineFourEvenWhenEnoughManaIsAvailable() {
        Permanent teammate = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        LlanowarElves elves = castTargetSpellWithThreeManaRemaining();
        harness.addMana(player1, ManaColor.GREEN, 1);

        castWeSayTheeNay(elves, List.of(teammate.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void teamworkCanCombinePowerOfSummoningSickCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        LlanowarElves elves = castTargetSpellWithThreeManaRemaining();

        castWeSayTheeNay(elves, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second);
    }

    @Test
    void canDeclineTwoWithoutTeamwork() {
        LlanowarElves elves = castTargetSpellWithTwoManaRemaining();

        castWeSayTheeNay(elves, List.of());

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    private LlanowarElves castTargetSpellWithTwoManaRemaining() {
        LlanowarElves elves = new LlanowarElves();
        harness.castFromHand(player1, elves, "{G}");
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        return elves;
    }

    private LlanowarElves castTargetSpellWithThreeManaRemaining() {
        LlanowarElves elves = new LlanowarElves();
        harness.castFromHand(player1, elves, "{G}");
        harness.addMana(player1, ManaColor.GREEN, 3);
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
