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

    @Test
    void targetedPlayerChoosesWhichCardToExile() {
        Permanent vessel = addVessel();
        Card creature = new MistralCharger();
        Card land = new GhostQuarter();
        harness.setHand(player2, List.of(creature, land));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.getCardsExiledByPermanent(vessel.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
    }

    @Test
    void mayPlayOpponentsCardButMustPayItsNormalColoredManaCost() {
        addVessel();
        Card creature = new MistralCharger();
        harness.setHand(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mistral Charger");
        harness.assertNotOnBattlefield(player2, "Mistral Charger");
    }

    @Test
    void choiceIncludesOnlyCardsExiledWithThisVessel() {
        Permanent vessel = addVessel();
        Permanent otherVessel = addVessel();
        Card creature = new MistralCharger();
        Card otherCard = new GhostQuarter();
        Card unlinkedCard = new MistralCharger();
        gd.addToExile(player2.getId(), creature, vessel.getId());
        gd.addToExile(player2.getId(), otherCard, otherVessel.getId());
        gd.addToExile(player2.getId(), unlinkedCard);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.ExiledCardMayPlayChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ExiledCardMayPlayChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(otherCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThatThrownBy(() -> harness.castFromExile(player1, otherCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void secondAbilityCanBeActivatedOnOpponentsTurnButDoesNotOverrideCreatureTiming() {
        Permanent vessel = addVessel();
        Card creature = new MistralCharger();
        gd.addToExile(player2.getId(), creature, vessel.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }

    @Test
    void canTargetSelfButExilingDoesNotYetGrantPlayPermission() {
        Permanent vessel = addVessel();
        Card creature = new MistralCharger();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getCardsExiledByPermanent(vessel.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permission");

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mistral Charger");
    }

    @Test
    void choosingLandDoesNotGrantAnAdditionalLandPlay() {
        Permanent vessel = addVessel();
        Card land = new GhostQuarter();
        gd.addToExile(player2.getId(), land, vessel.getId());
        harness.setHand(player1, List.of(new GhostQuarter()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot play a land");
        assertThat(gd.getCardsExiledByPermanent(vessel.getId())).containsExactly(land);
    }

    private Permanent addVessel() {
        return harness.addToBattlefieldAndReturn(player1, new MuseVessel());
    }
}
