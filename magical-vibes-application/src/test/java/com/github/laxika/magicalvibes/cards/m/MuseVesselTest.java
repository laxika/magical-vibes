package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MuseVessel.class, MistralCharger.class, GhostQuarter.class})
class MuseVesselTest extends BaseCardTest {

    @Test
    void targetPlayerExilesCardFromHandAndTracksItWithVessel() {
        Permanent vessel = addVessel();
        Card exiled = new MistralCharger();
        harness.setHand(player2, List.of(exiled));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.getCardsExiledByPermanent(vessel.getId())).containsExactly(exiled);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(vessel.isTapped()).isTrue();
    }

    @Test
    void choosesOneExiledCardAndMayPlayItThisTurn() {
        Permanent vessel = addVessel();
        Card creature = new MistralCharger();
        Card land = new GhostQuarter();
        gd.addToExile(player1.getId(), creature, vessel.getId());
        gd.addToExile(player1.getId(), land, vessel.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.ExiledCardMayPlayChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ExiledCardMayPlayChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId(), land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature);
        assertThat(gd.getCardsExiledByPermanent(vessel.getId())).containsExactly(land);
    }

    @Test
    void firstAbilityCanOnlyBeActivatedAsASorcery() {
        addVessel();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    @Test
    void targetWithEmptyHandHasNoCardToExile() {
        Permanent vessel = addVessel();
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(vessel.getId())).isEmpty();
        assertThat(vessel.isTapped()).isTrue();
    }

    @Test
    void secondAbilityDoesNotPromptWithoutCardsExiledWithVessel() {
        addVessel();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ExiledCardMayPlayChoice.class))
                .isNull();
    }

    @Test
    void choosesAnExiledLandAndMayPlayItThisTurn() {
        Permanent vessel = addVessel();
        Card land = new GhostQuarter();
        gd.addToExile(player1.getId(), land, vessel.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, land.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land);
        assertThat(gd.getCardsExiledByPermanent(vessel.getId())).isEmpty();
    }

    @Test
    void selectedCardCannotBePlayedAfterThisTurn() {
        Permanent vessel = addVessel();
        Card creature = new MistralCharger();
        gd.addToExile(player1.getId(), creature, vessel.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permission");
    }

    private Permanent addVessel() {
        return harness.addToBattlefieldAndReturn(player1, new MuseVessel());
    }
}
