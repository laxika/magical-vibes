package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HandOfVecna.class, GrizzlyBears.class})
class HandOfVecnaTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, equipped creature gets +X/+X for hand size")
    void boostsEquippedCreatureByHandSize() {
        Permanent hand = harness.addToBattlefieldAndReturn(player1, new HandOfVecna());
        Permanent bears = addCreatureReady(new GrizzlyBears());
        hand.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        enterBeginningOfCombat();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("The beginning-of-combat ability also boosts a creature named Vecna")
    void boostsVecnaEvenWhenUnequipped() {
        harness.addToBattlefield(player1, new HandOfVecna());
        Permanent vecna = addVecna();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        enterBeginningOfCombat();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vecna)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, vecna)).isEqualTo(10);
    }

    @Test
    @DisplayName("A Vecna gets only one boost when the Equipment is attached to it")
    void doesNotDoubleBoostAttachedVecna() {
        Permanent hand = harness.addToBattlefieldAndReturn(player1, new HandOfVecna());
        Permanent vecna = addVecna();
        hand.setAttachedTo(vecna.getId());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        enterBeginningOfCombat();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vecna)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, vecna)).isEqualTo(10);
    }

    @Test
    @DisplayName("The life equip ability pays one life for each card in hand")
    void lifeEquipPaysForHandSize() {
        Permanent hand = harness.addToBattlefieldAndReturn(player1, new HandOfVecna());
        Permanent bears = addCreatureReady(new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, bears.getId());

        harness.assertLife(player1, 17);
        harness.passBothPriorities();
        assertThat(hand.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("The life equip ability cannot be activated without enough life")
    void lifeEquipRequiresEnoughLife() {
        Permanent hand = harness.addToBattlefieldAndReturn(player1, new HandOfVecna());
        Permanent bears = addCreatureReady(new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
        assertThat(hand.getAttachedTo()).isNull();
    }

    private Permanent addCreatureReady(Card card) {
        return addCreatureReady(player1, card);
    }

    private Permanent addVecna() {
        Card vecna = new Card();
        vecna.setName("Vecna");
        vecna.setType(CardType.CREATURE);
        vecna.setPower(8);
        vecna.setToughness(8);
        return addCreatureReady(vecna);
    }

    private void enterBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}
