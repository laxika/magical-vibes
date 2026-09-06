package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfTheAges.class, GrizzlyBears.class})
class SwordOfTheAgesTest extends BaseCardTest {

    @Test
    void sacrificesChosenCreaturesDealsTheirTotalPowerAndExilesThem() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheAges());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sword, firstBear, secondBear);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(sword.getCard(), firstBear.getCard(), secondBear.getCard());
    }

    @Test
    void maySacrificeZeroCreatures() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheAges());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear).doesNotContain(sword);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(sword.getCard());
    }
}
