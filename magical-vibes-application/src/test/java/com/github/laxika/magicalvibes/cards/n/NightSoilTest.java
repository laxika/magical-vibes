package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NightSoilTest extends BaseCardTest {

    @Test
    void exilesTwoCreatureCardsFromOpponentGraveyardAndCreatesSaproling() {
        var nightSoil = harness.addToBattlefieldAndReturn(player1, new NightSoil());
        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        harness.setGraveyard(player2, List.of(bears, elves));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gs.canActivateAbility(gd, player1.getId(), nightSoil, 0,
                gd.playerManaPools.get(player1.getId()))).isTrue();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(bears, elves);
        harness.assertOnBattlefield(player1, "Saproling");
    }

    @Test
    void requiresBothCardsToComeFromTheSameGraveyard() {
        var nightSoil = harness.addToBattlefieldAndReturn(player1, new NightSoil());
        GrizzlyBears opponentCreature = new GrizzlyBears();
        LlanowarElves controllerCreature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(controllerCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gs.canActivateAbility(gd, player1.getId(), nightSoil, 0,
                gd.playerManaPools.get(player1.getId()))).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void rejectsNonCreatureCardsAsCostCards() {
        harness.addToBattlefield(player1, new NightSoil());
        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        Cancel cancel = new Cancel();
        harness.setGraveyard(player1, List.of(bears, elves, cancel));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), cancel.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears, elves, cancel);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }
}
