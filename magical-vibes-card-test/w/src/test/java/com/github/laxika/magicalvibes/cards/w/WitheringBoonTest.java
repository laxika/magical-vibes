package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitheringBoon.class, LlanowarElves.class, GrizzlyBears.class, MightOfOaks.class})
class WitheringBoonTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and costs 3 life")
    void countersCreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new WitheringBoon()));
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.assertLife(player2, 17);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot target a non-creature spell")
    void cannotTargetNonCreatureSpell() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new WitheringBoon()));
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, might.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot be cast without enough life to pay the additional cost")
    void cannotBeCastWithoutEnoughLife() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new WitheringBoon()));
        harness.setLife(player2, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertLife(player2, 2);
    }

    @Test
    @DisplayName("Can counter its controller's own creature spell")
    void countersOwnCreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves, new WitheringBoon()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, elves.getId());

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Withering Boon");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Life paid is not refunded when the creature spell leaves the stack")
    void lifeCostIsRetainedWhenTargetDisappears() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new WitheringBoon(), new WitheringBoon()));
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.castAndResolveInstant(player2, 0, elves.getId());
        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player1, "Llanowar Elves");

        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(2)
                .allSatisfy(card -> assertThat(card).isInstanceOf(WitheringBoon.class));
    }
}
