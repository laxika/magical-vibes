package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KeepsakeGorgon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PharikasSpawn.class, GrizzlyBears.class, KeepsakeGorgon.class})
class PharikasSpawnTest extends BaseCardTest {

    @Test
    void normalCastDoesNotGetEscapeBonusOrMakeOpponentsSacrifice() {
        harness.setHand(player1, List.of(new PharikasSpawn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new KeepsakeGorgon());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent spawn = findPermanent(player1, "Pharika's Spawn");
        assertThat(spawn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Keepsake Gorgon");
    }

    @Test
    void escapeExilesThreeCardsAddsCountersAndMakesEachOpponentSacrificeNonGorgonCreature() {
        PharikasSpawn spawn = new PharikasSpawn();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(spawn, first, second, third));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new KeepsakeGorgon());

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third);

        harness.passBothPriorities();
        Permanent escapedSpawn = findPermanent(player1, "Pharika's Spawn");
        assertThat(escapedSpawn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Keepsake Gorgon");
        harness.assertOnBattlefield(player1, "Pharika's Spawn");
    }

    @Test
    void escapeRequiresThreeOtherCardsInTheGraveyard() {
        harness.setGraveyard(player1, List.of(new PharikasSpawn(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }
}
