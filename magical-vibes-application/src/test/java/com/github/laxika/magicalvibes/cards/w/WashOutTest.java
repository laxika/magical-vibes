package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WashOutTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Wash Out awaits the caster's color choice")
    void resolvingAwaitsColorChoice() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castWashOut();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Returns every permanent of the chosen color and leaves other colors alone")
    void returnsPermanentsOfChosenColor() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player2, new Forest());
        castWashOut();

        harness.handleListChoice(player1, "GREEN");

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Merfolk of the Pearl Trident");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Wash Out");
    }

    @Test
    @DisplayName("Returns no permanents when the chosen color is absent")
    void absentColorReturnsNothing() {
        harness.addToBattlefield(player2, new HillGiant());
        castWashOut();

        harness.handleListChoice(player1, "GREEN");

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Wash Out");
    }

    private void castWashOut() {
        harness.setHand(player1, List.of(new WashOut()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
