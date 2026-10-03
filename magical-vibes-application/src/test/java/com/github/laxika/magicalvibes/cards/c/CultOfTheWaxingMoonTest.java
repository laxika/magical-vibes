package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DaybreakRanger;
import com.github.laxika.magicalvibes.cards.m.Moonmist;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CultOfTheWaxingMoon.class, DaybreakRanger.class, Moonmist.class, Xenograft.class})
class CultOfTheWaxingMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Wolf when an ally transforms into a non-Human creature")
    void createsWolfWhenAllyTransformsIntoNonHumanCreature() {
        addReadyCult();
        Permanent ranger = addReadyRanger(player1);

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
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
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Wolf")).hasSize(1);

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUpkeep(player2);
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
        advanceToUpkeep(player2);
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
        harness.castAndResolveInstant(player1, 0);
        resolveAllTriggers();

        assertThat(ranger.isTransformed()).isTrue();
        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
    }

    @Test
    @DisplayName("Creates one Wolf for each ally transformed by Moonmist")
    void createsWolfForEachMassTransformation() {
        addReadyCult();
        Permanent first = addReadyRanger(player1);
        Permanent second = addReadyRanger(player1);
        Permanent opposing = addReadyRanger(player2);

        harness.setHand(player1, List.of(new Moonmist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
        resolveAllTriggers();

        assertThat(first.isTransformed()).isTrue();
        assertThat(second.isTransformed()).isTrue();
        assertThat(opposing.isTransformed()).isTrue();
        assertThat(findPermanents(player1, "Wolf")).hasSize(2);
        assertThat(findPermanents(player2, "Wolf")).isEmpty();
    }

    @Test
    @DisplayName("Each Cult triggers independently for an allied transformation")
    void eachCultCreatesWolf() {
        addReadyCult();
        addReadyCult();
        Permanent ranger = addReadyRanger(player1);

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(ranger.isTransformed()).isTrue();
        assertThat(findPermanents(player1, "Wolf")).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger when Xenograft keeps the transformed creature Human")
    void doesNotTriggerWhenTransformedCreatureRemainsHuman() {
        addReadyCult();
        Permanent ranger = addReadyRanger(player1);
        harness.setHand(player1, List.of(new Xenograft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "HUMAN");

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(ranger.isTransformed()).isTrue();
        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }

    private void addReadyCult() {
        addCreatureReady(player1, new CultOfTheWaxingMoon());
    }

    private Permanent addReadyRanger(Player player) {
        return addCreatureReady(player, new DaybreakRanger());
    }

}
