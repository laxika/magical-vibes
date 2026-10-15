package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagmaticChanneler.class, Divination.class, GrizzlyBears.class, Shock.class, Mountain.class,
        TormentingVoice.class})
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
        Permanent channeler = addCreatureReady(player1, new MagmaticChanneler());

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

    @Test
    void boostUpdatesWhenDiscardCostReachesThresholdAndWhenGraveyardShrinks() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Shock()));
        Permanent channeler = addCreatureReady(player1, new MagmaticChanneler());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, channeler)).isEqualTo(1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gqs.getEffectivePower(gd, channeler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, channeler)).isEqualTo(4);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Shock()));
        assertThat(gqs.getEffectivePower(gd, channeler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, channeler)).isEqualTo(3);
    }

    @Test
    @CardUsed({MagmaticChanneler.class, Mountain.class})
    void canChooseAndPlayLandWhenOnlyOneCardRemainsInLibrary() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addCreatureReady(player1, new MagmaticChanneler());
        Card land = new Mountain();
        harness.setHand(player1, List.of(new MagmaticChanneler()));
        harness.setLibrary(player1, List.of(land));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void chosenInstantCanBeCastByPayingItsNormalManaCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addCreatureReady(player1, new MagmaticChanneler());
        Card chosen = new Shock();
        Card unchosen = new MagmaticChanneler();
        harness.setHand(player1, List.of(new MagmaticChanneler()));
        harness.setLibrary(player1, List.of(chosen, unchosen));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, chosen.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(unchosen);
    }

    @Test
    @CardUsed({MagmaticChanneler.class, TormentingVoice.class, Mountain.class})
    void chosenSpellWithAdditionalDiscardCostCanBeCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addCreatureReady(player1, new MagmaticChanneler());
        Card chosen = new TormentingVoice();
        Card drawOne = new Mountain();
        Card drawTwo = new Mountain();
        harness.setHand(player1, List.of(new MagmaticChanneler(), new Mountain()));
        harness.setLibrary(player1, List.of(chosen, new MagmaticChanneler(), drawOne, drawTwo));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, chosen.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Tormenting Voice");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawOne, drawTwo);
    }
}
