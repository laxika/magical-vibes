package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrapEssence.class, LlanowarElves.class, GrizzlyBears.class, MightOfOaks.class,
        FountainOfYouth.class, Cancel.class, Unsummon.class})
class TrapEssenceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and puts two +1/+1 counters on a creature")
    void countersCreatureSpellAndBoostsCreature() {
        LlanowarElves elves = new LlanowarElves();

        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);
        harness.setHand(player2, List.of(new TrapEssence()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castFromHand(player1, elves, "{G}");
        harness.passPriority(player1);
        harness.castSorcery(player2, 0, elves.getId(),
                List.of(harness.getPermanentId(player2, "Grizzly Bears")));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        Permanent bear = findPermanent(player2, "Grizzly Bears");
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can omit the creature target")
    void canOmitCreatureTarget() {
        LlanowarElves elves = new LlanowarElves();

        harness.setHand(player2, List.of(new TrapEssence()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castFromHand(player1, elves, "{G}");
        harness.passPriority(player1);
        harness.castSorcery(player2, 0, elves.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target a noncreature spell")
    void cannotTargetNoncreatureSpell() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new TrapEssence()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, might.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent for the counters")
    void cannotTargetNoncreaturePermanent() {
        LlanowarElves elves = new LlanowarElves();

        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player2, List.of(new TrapEssence()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castFromHand(player1, elves, "{G}");
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, elves.getId(),
                List.of(harness.getPermanentId(player2, "Fountain of Youth"))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still puts counters on the creature when the creature spell has already been countered")
    void boostsCreatureWhenSpellTargetLeavesStack() {
        LlanowarElves elves = new LlanowarElves();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new TrapEssence()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castFromHand(player1, elves, "{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId(), harness.getPermanentId(player2, "Grizzly Bears"));

        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, elves.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(findPermanent(player2, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
        harness.assertInGraveyard(player2, "Trap Essence");
    }

    @Test
    @DisplayName("Still counters the creature spell when the creature target leaves the battlefield")
    void countersSpellWhenCreatureTargetLeavesBattlefield() {
        LlanowarElves elves = new LlanowarElves();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new TrapEssence()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castFromHand(player1, elves, "{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId(), harness.getPermanentId(player2, "Grizzly Bears"));

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Trap Essence");
    }

    @Test
    @DisplayName("Can put the counters on an opponent's creature")
    void canBoostOpponentsCreature() {
        LlanowarElves elves = new LlanowarElves();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new TrapEssence()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castFromHand(player1, elves, "{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId(), harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Requires a creature spell even when a creature is available for counters")
    void cannotCastWithoutCreatureSpellTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TrapEssence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, null,
                List.of(harness.getPermanentId(player1, "Grizzly Bears"))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose two creatures for the counters")
    void cannotTargetMoreThanOneCreatureForCounters() {
        LlanowarElves elves = new LlanowarElves();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new TrapEssence()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castFromHand(player1, elves, "{G}");
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, elves.getId(),
                List.of(harness.getPermanentId(player1, "Grizzly Bears"),
                        harness.getPermanentId(player2, "Grizzly Bears"))))
                .isInstanceOf(IllegalStateException.class);
    }
}
