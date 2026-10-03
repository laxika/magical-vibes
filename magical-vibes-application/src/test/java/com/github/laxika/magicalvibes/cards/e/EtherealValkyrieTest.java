package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtherealValkyrie.class, Forest.class, GrizzlyBears.class})
class EtherealValkyrieTest extends BaseCardTest {

    @Test
    void entersDrawsThenExilesAndForetellsAHandCard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new EtherealValkyrie());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);

        harness.handleCardChosen(player1, 0);

        ExiledCardEntry exiled = gd.findExiledCard(bears.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(gd.foretoldCardIds).contains(bears.getId());

        gd.turnNumber++;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void attackingDrawsThenExilesAndForetellsAHandCard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefieldAndReturn(player1, new EtherealValkyrie()).setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.foretoldCardIds).contains(bears.getId());
        assertThat(gd.findExiledCard(bears.getId()).faceDown()).isTrue();
    }
}
