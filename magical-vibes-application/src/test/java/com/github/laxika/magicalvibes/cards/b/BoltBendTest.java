package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoltBend.class, AirElemental.class, Boomerang.class, GrizzlyBears.class,
        LlanowarElves.class, ProdigalSorcerer.class})
class BoltBendTest extends BaseCardTest {

    @Test
    void retargetsSingleTargetSpellWithReducedCost() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID originalTarget = harness.getPermanentId(player1, "Grizzly Bears");
        UUID newTarget = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow().getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new BoltBend()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, originalTarget);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, newTarget);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void retargetsSingleTargetAbility() {
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
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
        harness.castInstant(player2, 0, abilityId);
        harness.passBothPriorities();

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
}
