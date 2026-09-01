package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Prizefight.class, GrizzlyBears.class, LlanowarElves.class})
class PrizefightTest extends BaseCardTest {

    @Test
    void fightsAndCreatesTreasure() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Prizefight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingCreatureId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(ownCreatureId, opposingCreatureId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void createsTreasureWhenOneFightTargetIsRemovedBeforeResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Prizefight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingCreatureId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(ownCreatureId, opposingCreatureId));
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        Permanent ownCreature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void cannotTargetOpponentCreatureAsCreatureYouControlTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Prizefight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingCreatureId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(opposingCreatureId, ownCreatureId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }
}
