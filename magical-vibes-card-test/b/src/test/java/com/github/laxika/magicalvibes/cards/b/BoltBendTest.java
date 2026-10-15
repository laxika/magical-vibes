package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.ChandrasPyrohelix;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoltBend.class, AirElemental.class, Boomerang.class, GrizzlyBears.class,
        LlanowarElves.class, ProdigalSorcerer.class, ChandrasPyrohelix.class, GiantGrowth.class})
class BoltBendTest extends BaseCardTest {

    @Test
    void retargetsSingleTargetSpellWithReducedCost() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID originalTarget = harness.getPermanentId(player1, "Grizzly Bears");
        UUID newTarget = harness.getPermanentId(player2, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new BoltBend()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, originalTarget);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, boomerang.getId());

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, newTarget);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void retargetsSingleTargetAbility() {
        addCreatureReady(player1, new ProdigalSorcerer());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID originalTarget = harness.getPermanentId(player1, "Llanowar Elves");
        UUID newTarget = harness.getPermanentId(player2, "Llanowar Elves");

        harness.setHand(player2, List.of(new BoltBend()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, originalTarget);
        UUID abilityId = harness.getGameData().stack.getFirst().getCard().getId();
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, abilityId);

        harness.handlePermanentChosen(player2, newTarget);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    void requiresFullCostWithoutPowerfulCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID target = harness.getPermanentId(player1, "Grizzly Bears");
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new BoltBend()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, boomerang.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void retargetsDividedDamageWithOneTargetToAnotherPlayerAtFullCost() {
        ChandrasPyrohelix pyrohelix = new ChandrasPyrohelix();
        harness.setHand(player1, List.of(pyrohelix));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new BoltBend()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, pyrohelix.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotTargetSpellWithTwoTargets() {
        ChandrasPyrohelix pyrohelix = new ChandrasPyrohelix();
        harness.setHand(player1, List.of(pyrohelix));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new BoltBend()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castInstant(player1, 0, Map.of(player1.getId(), 1, player2.getId(), 1));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, pyrohelix.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetSpellWithoutTargets() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new BoltBend()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mustChooseDifferentLegalCreatureForCreatureOnlySpell() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        UUID originalTarget = harness.getPermanentId(player1, "Grizzly Bears");
        UUID newTarget = harness.getPermanentId(player2, "Air Elemental");
        GiantGrowth growth = new GiantGrowth();
        harness.setHand(player1, List.of(growth));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new BoltBend()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player1, 0, originalTarget);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, growth.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, originalTarget))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player2, newTarget);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Air Elemental"))).isEqualTo(7);
    }

    @Test
    void opponentsPowerfulCreatureDoesNotReduceCost() {
        harness.addToBattlefield(player1, new AirElemental());
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new BoltBend()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Air Elemental"));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, boomerang.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void usesCurrentPowerForCostReduction() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bears = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, bears);

        ChandrasPyrohelix pyrohelix = new ChandrasPyrohelix();
        harness.setHand(player1, List.of(pyrohelix));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new BoltBend()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, pyrohelix.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void leavesTargetUnchangedWhenNoLegalAlternativeExists() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bears = harness.getPermanentId(player1, "Grizzly Bears");
        GiantGrowth growth = new GiantGrowth();
        harness.setHand(player1, List.of(growth));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new BoltBend()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castInstant(player1, 0, bears);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, growth.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears")))
                .isEqualTo(5);
        harness.assertInGraveyard(player2, "Bolt Bend");
    }
}
