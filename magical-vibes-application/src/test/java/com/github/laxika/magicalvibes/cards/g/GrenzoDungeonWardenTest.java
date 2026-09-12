package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrenzoDungeonWarden.class, Forest.class, HillGiant.class})
class GrenzoDungeonWardenTest extends BaseCardTest {

    @Test
    void entersWithTheChosenNumberOfPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new GrenzoDungeonWarden()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent grenzo = findPermanent(player1, "Grenzo, Dungeon Warden");
        assertThat(grenzo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void putsBottomCreatureOntoBattlefieldWhenItsPowerIsAtMostGrenzos() {
        Permanent grenzo = addReadyGrenzo();
        grenzo.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new Forest(), new HillGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotInGraveyard(player1, "Hill Giant");
    }

    @Test
    void leavesBottomCreatureInGraveyardWhenItsPowerIsTooHigh() {
        addReadyGrenzo();
        harness.setLibrary(player1, List.of(new Forest(), new HillGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    private Permanent addReadyGrenzo() {
        return addCreatureReady(player1, new GrenzoDungeonWarden());
    }
}
