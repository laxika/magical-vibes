package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.y.YuyanArchers;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ItllQuenchYa.class, GrizzlyBears.class, LlanowarElves.class, Island.class, YuyanArchers.class})
class ItllQuenchYaTest extends BaseCardTest {

    @Test
    void countersSpellWhenControllerCannotPay() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new ItllQuenchYa()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void spellResolvesWhenControllerPaysTwo() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.setHand(player2, List.of(new ItllQuenchYa()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void fizzlesIfTargetSpellLeavesTheStack() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ItllQuenchYa()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(stackEntry -> stackEntry.getCard().getId().equals(bears.getId()));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "It'll Quench Ya!");
    }

    @Test
    void countersSpellWhenControllerDeclinesPayment() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new ItllQuenchYa()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");

    }

    @Test
    void canCounterAnotherInstantSpell() {
        YuyanArchers archers = new YuyanArchers();
        ItllQuenchYa firstCounter = new ItllQuenchYa();
        harness.setHand(player1, List.of(archers, new ItllQuenchYa()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(firstCounter));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, archers.getId());
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, firstCounter.getId());

        harness.assertInGraveyard(player2, "It'll Quench Ya!");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(firstCounter.getId()));
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(archers.getId()));
    }

    @Test
    void allowsGeneratingManaDuringResolutionBeforePayment() {
        YuyanArchers archers = new YuyanArchers();
        harness.setHand(player1, List.of(archers));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player2, List.of(new ItllQuenchYa()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, archers.getId());

        harness.assertNotInGraveyard(player1, "Yuyan Archers");
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(archers.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
