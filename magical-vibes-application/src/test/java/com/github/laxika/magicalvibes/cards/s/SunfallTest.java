package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sunfall.class, GrizzlyBears.class, Island.class})
class SunfallTest extends BaseCardTest {

    @Test
    void exilesCreaturesAndIncubatesForEachCreatureExiled() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());

        cast();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Island");

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void incubatorCanTransformAfterResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        cast();

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void cast() {
        harness.setHand(player1, List.of(new Sunfall()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
