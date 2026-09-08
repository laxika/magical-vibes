package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DaybreakRanger;
import com.github.laxika.magicalvibes.cards.m.Moonmist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CultOfTheWaxingMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Wolf when an ally transforms into a non-Human creature")
    void createsWolfWhenAllyTransformsIntoNonHumanCreature() {
        addReadyCult();
        Permanent ranger = addReadyRanger(player1);

        gd.spellsCastLastTurn.clear();
        resolveUpkeepTransform(player1);
        resolveAllTriggers();

        assertThat(ranger.isTransformed()).isTrue();
        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when an ally transforms into a Human creature")
    void doesNotTriggerWhenAllyTransformsIntoHumanCreature() {
        addReadyCult();
        Permanent ranger = addReadyRanger(player1);

        gd.spellsCastLastTurn.clear();
        resolveUpkeepTransform(player1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Wolf")).hasSize(1);

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        resolveUpkeepTransform(player2);
        resolveAllTriggers();

        assertThat(ranger.isTransformed()).isFalse();
        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's transformation")
    void doesNotTriggerForOpponentsTransformation() {
        addReadyCult();
        Permanent ranger = addReadyRanger(player2);

        gd.spellsCastLastTurn.clear();
        resolveUpkeepTransform(player2);
        resolveAllTriggers();

        assertThat(ranger.isTransformed()).isTrue();
        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }

    @Test
    @DisplayName("Creates a Wolf when an ally is transformed by a mass effect")
    void createsWolfForMassTransform() {
        addReadyCult();
        Permanent ranger = addReadyRanger(player1);

        harness.setHand(player1, List.of(new Moonmist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(ranger.isTransformed()).isTrue();
        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
    }

    private void addReadyCult() {
        addCreatureReady(player1, new CultOfTheWaxingMoon());
    }

    private Permanent addReadyRanger(Player player) {
        return addCreatureReady(player, new DaybreakRanger());
    }

    private void resolveUpkeepTransform(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
