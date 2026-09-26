package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfTheAges.class, BarbaryApes.class})
class SwordOfTheAgesTest extends BaseCardTest {

    @Test
    void entersBattlefieldTapped() {
        Permanent sword = harness.enterBattlefieldAndReturn(player1, new SwordOfTheAges());

        assertThat(sword.isTapped()).isTrue();
    }

    @Test
    void sacrificesChosenCreaturesDealsTheirTotalPowerAndExilesThem() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheAges());
        Permanent firstApe = addCreatureReady(player1, new BarbaryApes());
        Permanent secondApe = addCreatureReady(player1, new BarbaryApes());
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstApe.getId(), secondApe.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sword, firstApe, secondApe);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(sword.getCard(), firstApe.getCard(), secondApe.getCard());
    }

    @Test
    void dealsDamageToCreatureTarget() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheAges());
        Permanent sacrificedApe = addCreatureReady(player1, new BarbaryApes());
        Permanent targetApe = addCreatureReady(player2, new BarbaryApes());

        harness.activateAbility(player1, 0, null, targetApe.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(sacrificedApe.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(targetApe);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(targetApe.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(sword.getCard(), sacrificedApe.getCard());
    }

    @Test
    void maySacrificeZeroCreatures() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheAges());
        Permanent ape = addCreatureReady(player1, new BarbaryApes());
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ape).doesNotContain(sword);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(sword.getCard());
    }
}
