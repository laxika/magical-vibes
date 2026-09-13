package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagmaticChanneler.class, Divination.class, GrizzlyBears.class, Shock.class})
class MagmaticChannelerTest extends BaseCardTest {

    @Test
    void getsPlusThreePlusOneWithFourInstantOrSorceryCardsInControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock(), new Divination()));
        Permanent channeler = harness.addToBattlefieldAndReturn(player1, new MagmaticChanneler());

        assertThat(gqs.getEffectivePower(gd, channeler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, channeler)).isEqualTo(4);
    }

    @Test
    void doesNotCountCreaturesOrOpponentsGraveyardForBoost() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Divination(), new Shock(), new Divination(), new Shock()));
        Permanent channeler = harness.addToBattlefieldAndReturn(player1, new MagmaticChanneler());

        assertThat(gqs.getEffectivePower(gd, channeler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, channeler)).isEqualTo(3);
    }

    @Test
    void discardsExilesTopTwoAndLetsControllerPlayChosenCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent channeler = harness.addToBattlefieldAndReturn(player1, new MagmaticChanneler());
        channeler.setSummoningSick(false);

        Card discarded = new GrizzlyBears();
        Card chosen = new Shock();
        Card notChosen = new Divination();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(chosen, notChosen));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(channeler.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen, notChosen);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.exilePlayPermissions.get(chosen.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(notChosen.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(chosen.getId());
    }
}
