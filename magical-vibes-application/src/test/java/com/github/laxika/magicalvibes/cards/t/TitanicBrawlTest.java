package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TitanicBrawlTest extends BaseCardTest {

    @Test
    @DisplayName("The targeted creatures fight")
    void targetedCreaturesFight() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new TitanicBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, List.of(bearId, giantId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The spell costs only {G} when its first target has a +1/+1 counter")
    void counterBearingTargetReducesCost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new TitanicBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = bears.getId();
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("The spell requires its full cost without a +1/+1 counter")
    void noCostReductionWithoutCounter() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new TitanicBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The first target must be a creature you control")
    void firstTargetMustBeControlledCreature() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new TitanicBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID opponentBearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID opponentElvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opponentBearId, opponentElvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }
}
