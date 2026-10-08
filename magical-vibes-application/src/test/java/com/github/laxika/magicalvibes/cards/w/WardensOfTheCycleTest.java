package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WardensOfTheCycle.class, Forest.class, LlanowarElves.class})
class WardensOfTheCycleTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger when no creature died this turn")
    void doesNotTriggerWithoutMorbid() {
        harness.addToBattlefield(player1, new WardensOfTheCycle());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("Morbid mode gains 2 life")
    void gainsLifeWithMorbid() {
        harness.addToBattlefield(player1, new WardensOfTheCycle());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);
        int lifeBefore = gd.getLife(player1.getId());

        advanceToEndStep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "You gain 2 life.");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Morbid draw mode draws a card and loses 1 life")
    void drawsAndLosesLifeWithMorbid() {
        harness.addToBattlefield(player1, new WardensOfTheCycle());
        harness.setLibrary(player1, List.of(new Forest()));
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());

        advanceToEndStep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "You draw a card and you lose 1 life.");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step even with morbid")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new WardensOfTheCycle());
        killElf(player2);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("Multiple real creature deaths produce only one end-step trigger")
    void multipleDeathsProduceOneTrigger() {
        harness.addToBattlefield(player1, new WardensOfTheCycle());
        killElf(player1);
        killElf(player2);
        int lifeBefore = gd.getLife(player1.getId());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "You gain 2 life.");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("A creature dying after the end step begins does not trigger the ability")
    void deathAfterEndStepBeginsDoesNotTrigger() {
        harness.addToBattlefield(player1, new WardensOfTheCycle());
        advanceToEndStep(player1);

        killElf(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("The chosen ability resolves even if Wardens dies in response")
    void resolvesAfterSourceDies() {
        var wardens = harness.addToBattlefieldAndReturn(player1, new WardensOfTheCycle());
        killElf(player2);
        int lifeBefore = gd.getLife(player1.getId());
        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "You gain 2 life.");
        wardens.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wardens);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    private void killElf(Player player) {
        var elf = harness.addToBattlefieldAndReturn(player, new LlanowarElves());
        elf.setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(gd.playerGraveyards.get(player.getId())).contains(elf.getCard());
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
