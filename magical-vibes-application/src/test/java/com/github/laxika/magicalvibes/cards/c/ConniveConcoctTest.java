package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.v.VeiledShade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConniveConcoct.class, VeiledShade.class, BartizanBats.class, Island.class})
class ConniveConcoctTest extends BaseCardTest {

    @Test
    @DisplayName("Connive gains permanent control of a creature with power 2 or less")
    void conniveGainsControlOfSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VeiledShade());

        harness.setHand(player1, List.of(new ConniveConcoct()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Connive cannot target a creature with power greater than 2")
    void conniveRejectsLargeCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BartizanBats());

        harness.setHand(player1, List.of(new ConniveConcoct()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Concoct surveils 3, then returns a creature from your graveyard")
    void concoctSurveilsThenReturnsCreature() {
        Card topCard = new Island();
        Card secondCard = new VeiledShade();
        Card thirdCard = new Island();
        Card creature = new VeiledShade();
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard));
        harness.setGraveyard(player1, List.of(creature));

        harness.setHand(player1, List.of(new ConniveConcoct()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 1);

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard, thirdCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        PendingInteraction.GraveyardChoice graveyardChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(graveyardChoice).isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).contains(creature);
        harness.assertNotInGraveyard(player1, "Veiled Shade");
    }

    @Test
    @DisplayName("Connive can be paid entirely with black mana")
    void conniveAcceptsBlackHybridPayment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VeiledShade());
        harness.setHand(player1, List.of(new ConniveConcoct()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Connive does not resolve if its target grows above power 2")
    void conniveRechecksPowerAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VeiledShade());
        harness.setHand(player1, List.of(new ConniveConcoct()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 0, target.getId());

        target.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Connive's control persists when the creature grows after resolution")
    void conniveControlPersistsAfterPowerIncreases() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VeiledShade());
        harness.setHand(player1, List.of(new ConniveConcoct()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        target.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.publishState();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Concoct can return a creature just surveilled without another priority round")
    void concoctReturnsNewlySurveilledCreatureDuringSameResolution() {
        Card creature = new VeiledShade();
        Card land = new Island();
        harness.setLibrary(player1, List.of(creature, land));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new ConniveConcoct()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 1);
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(creature, land);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Concoct cannot decline returning an available creature")
    void concoctMustReturnAvailableCreature() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new VeiledShade()));
        harness.setHand(player1, List.of(new ConniveConcoct()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Concoct still surveils when only the opponent has a creature in their graveyard")
    void concoctSurveilsWithoutCreatureToReturn() {
        Card land = new Island();
        harness.setLibrary(player1, List.of(land));
        harness.setGraveyard(player1, List.of(new Island()));
        harness.setGraveyard(player2, List.of(new VeiledShade()));
        harness.setHand(player1, List.of(new ConniveConcoct()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        harness.assertNotOnBattlefield(player1, "Veiled Shade");
        harness.assertInGraveyard(player2, "Veiled Shade");
    }
}
