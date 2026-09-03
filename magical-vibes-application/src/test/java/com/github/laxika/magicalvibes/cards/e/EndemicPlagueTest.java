package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EndemicPlague.class, GrizzlyBears.class, LlanowarElves.class, RagingGoblin.class})
class EndemicPlagueTest extends BaseCardTest {

    @Test
    void destroysAllCreaturesSharingTheSacrificedCreatureType() {
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player1, new RagingGoblin());
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Raging Goblin");
    }

    @Test
    void destructionDoesNotAllowRegeneration() {
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        matchingCreature.setRegenerationShield(1);
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotCastWithoutACreatureToSacrifice() {
        harness.setHand(player1, List.of(new EndemicPlague()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new EndemicPlague()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
