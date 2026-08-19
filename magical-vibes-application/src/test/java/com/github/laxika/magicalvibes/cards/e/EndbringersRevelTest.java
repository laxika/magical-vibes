package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EndbringersRevelTest extends BaseCardTest {

    @Test
    @DisplayName("Any player may return a creature card from any graveyard to its owner's hand")
    void anyPlayerMayReturnCreatureFromAnyGraveyard() {
        int revelIndex = addRevelIndex();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        prepareForSorcerySpeedActivation(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player2, revelIndex, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature card returns to its owner's hand")
    void returnsCreatureToItsOwnersHand() {
        int revelIndex = addRevelIndex();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        prepareForSorcerySpeedActivation(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, revelIndex, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void requiresSorcerySpeed() {
        int revelIndex = addRevelIndex();
        Card creature = new GrizzlyBears();
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
    @DisplayName("Can only target creature cards")
    void rejectsNonCreatureTarget() {
        int revelIndex = addRevelIndex();
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(nonCreature));
        prepareForSorcerySpeedActivation(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, revelIndex, 0, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
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
