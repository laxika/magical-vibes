package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EndbringersRevel.class, DivingGriffin.class, RhysticCave.class})
class EndbringersRevelTest extends BaseCardTest {

    @Test
    @DisplayName("Any player may return a creature card from any graveyard to its owner's hand")
    void anyPlayerMayReturnCreatureFromAnyGraveyard() {
        int revelIndex = addRevelIndex();
        Card creature = new DivingGriffin();
        harness.setGraveyard(player2, List.of(creature));
        prepareForSorcerySpeedActivation(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player2, revelIndex, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Diving Griffin");
        harness.assertNotInGraveyard(player2, "Diving Griffin");
    }

    @Test
    @DisplayName("A creature card returns to its owner's hand")
    void returnsCreatureToItsOwnersHand() {
        int revelIndex = addRevelIndex();
        Card creature = new DivingGriffin();
        harness.setGraveyard(player2, List.of(creature));
        prepareForSorcerySpeedActivation(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, revelIndex, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Diving Griffin");
        harness.assertNotInGraveyard(player2, "Diving Griffin");
    }

    @Test
    @DisplayName("Can return a creature card from the Revel controller's graveyard")
    void returnsCreatureFromControllersGraveyard() {
        int revelIndex = addRevelIndex();
        Card creature = new DivingGriffin();
        harness.setGraveyard(player1, List.of(creature));
        prepareForSorcerySpeedActivation(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player2, revelIndex, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Diving Griffin");
        harness.assertNotInGraveyard(player1, "Diving Griffin");
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void requiresSorcerySpeed() {
        int revelIndex = addRevelIndex();
        Card creature = new DivingGriffin();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, revelIndex, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("An opponent cannot activate it during the Revel controller's turn")
    void opponentCannotActivateDuringControllersTurn() {
        int revelIndex = addRevelIndex();
        Card creature = new DivingGriffin();
        harness.setGraveyard(player2, List.of(creature));
        prepareForSorcerySpeedActivation(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player2, revelIndex, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Can only target creature cards")
    void rejectsNonCreatureTarget() {
        int revelIndex = addRevelIndex();
        Card nonCreature = new RhysticCave();
        harness.setGraveyard(player1, List.of(nonCreature));
        prepareForSorcerySpeedActivation(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, revelIndex, 0, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without paying four generic mana")
    void requiresFourGenericMana() {
        int revelIndex = addRevelIndex();
        Card creature = new DivingGriffin();
        harness.setGraveyard(player1, List.of(creature));
        prepareForSorcerySpeedActivation(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, revelIndex, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while another ability is on the stack")
    void requiresEmptyStack() {
        int revelIndex = addRevelIndex();
        Card creature = new DivingGriffin();
        harness.setGraveyard(player1, List.of(creature));
        prepareForSorcerySpeedActivation(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbilityWithGraveyardTargets(player1, revelIndex, 0, List.of(creature.getId()));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, revelIndex, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    @DisplayName("Does not return a target that has left the graveyard")
    void targetLeavingGraveyardIsNotReturned() {
        int revelIndex = addRevelIndex();
        Card creature = new DivingGriffin();
        harness.setGraveyard(player2, List.of(creature));
        prepareForSorcerySpeedActivation(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, revelIndex, 0, List.of(creature.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Diving Griffin");
        harness.assertNotInHand(player2, "Diving Griffin");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(creature);
    }

    @Test
    @DisplayName("An activated ability resolves after the Revel leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        int revelIndex = addRevelIndex();
        Card creature = new DivingGriffin();
        harness.setGraveyard(player2, List.of(creature));
        prepareForSorcerySpeedActivation(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player2, revelIndex, 0, List.of(creature.getId()));
        Card revel = gd.playerBattlefields.get(player1.getId()).remove(revelIndex).getCard();
        harness.setGraveyard(player1, List.of(revel));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Diving Griffin");
        harness.assertNotInGraveyard(player2, "Diving Griffin");
        harness.assertInGraveyard(player1, "Endbringer's Revel");
    }

    @Test
    @DisplayName("Can activate repeatedly in the postcombat main phase with colored mana")
    void repeatedPostcombatActivationsWithColoredMana() {
        int revelIndex = addRevelIndex();
        Card first = new DivingGriffin();
        Card second = new DivingGriffin();
        harness.setGraveyard(player2, List.of(first, second));
        prepareForSorcerySpeedActivation(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player2, ManaColor.WHITE, 8);

        harness.activateAbilityWithGraveyardTargets(player2, revelIndex, 0, List.of(first.getId()));
        harness.passBothPriorities();
        harness.activateAbilityWithGraveyardTargets(player2, revelIndex, 0, List.of(second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .contains(first, second);
        harness.assertNotInGraveyard(player2, "Diving Griffin");
    }

    private int addRevelIndex() {
        Permanent revel = harness.addToBattlefieldAndReturn(player1, new EndbringersRevel());
        return gd.playerBattlefields.get(player1.getId()).indexOf(revel);
    }

    private void prepareForSorcerySpeedActivation(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
