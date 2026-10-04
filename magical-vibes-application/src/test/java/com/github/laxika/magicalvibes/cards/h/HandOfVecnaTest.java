package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EyeOfVecna;
import com.github.laxika.magicalvibes.cards.t.TheBookOfVileDarkness;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HandOfVecna.class, HillGiantHerdgorger.class, EyeOfVecna.class, TheBookOfVileDarkness.class})
class HandOfVecnaTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, equipped creature gets +X/+X for hand size")
    void boostsEquippedCreatureByHandSize() {
        Permanent hand = harness.addToBattlefieldAndReturn(player1, new HandOfVecna());
        Permanent bears = addCreatureReady(new HillGiantHerdgorger());
        hand.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger(), new HillGiantHerdgorger()));

        enterBeginningOfCombat();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(9);
    }

    @Test
    @DisplayName("The beginning-of-combat ability also boosts a creature named Vecna")
    void boostsVecnaEvenWhenUnequipped() {
        harness.addToBattlefield(player1, new HandOfVecna());
        Permanent vecna = addVecna();
        harness.setHand(player1, List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger()));

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
        harness.setHand(player1, List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger()));

        enterBeginningOfCombat();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vecna)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, vecna)).isEqualTo(10);
    }

    @Test
    @DisplayName("The life equip ability pays one life for each card in hand")
    void lifeEquipPaysForHandSize() {
        Permanent hand = harness.addToBattlefieldAndReturn(player1, new HandOfVecna());
        Permanent bears = addCreatureReady(new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger(), new HillGiantHerdgorger()));
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
        Permanent bears = addCreatureReady(new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger(), new HillGiantHerdgorger()));
        harness.setLife(player1, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
        assertThat(hand.getAttachedTo()).isNull();
    }

    @Test
    void choosesOnlyOneCreatureWhenVecnaAndAnotherEquippedCreatureAreAvailable() {
        Permanent hand = harness.addToBattlefieldAndReturn(player1, new HandOfVecna());
        Permanent equipped = addCreatureReady(new HillGiantHerdgorger());
        Permanent vecna = addVecna();
        hand.setAttachedTo(equipped.getId());
        harness.setHand(player1, List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger()));

        enterBeginningOfCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, equipped.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, equipped)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, equipped)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, vecna)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, vecna)).isEqualTo(8);
    }

    @Test
    void canChooseVecnaThatEntersAfterTheAbilityTriggers() {
        harness.addToBattlefield(player1, new HandOfVecna());
        harness.setHand(player1, List.of(new HillGiantHerdgorger()));
        enterBeginningOfCombat();
        Permanent vecna = addVecna();

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vecna)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, vecna)).isEqualTo(9);
    }

    @Test
    void usesHandSizeAtResolutionAndKeepsTheBonusWhenHandSizeChangesLater() {
        Permanent hand = harness.addToBattlefieldAndReturn(player1, new HandOfVecna());
        Permanent equipped = addCreatureReady(new HillGiantHerdgorger());
        hand.setAttachedTo(equipped.getId());
        harness.setHand(player1, List.of(new HillGiantHerdgorger()));
        enterBeginningOfCombat();
        harness.setHand(player1, List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger()));

        resolveAllTriggers();
        harness.setHand(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, equipped)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, equipped)).isEqualTo(8);
    }

    @Test
    void doesNotBoostCreaturesOnOpponentsCombat() {
        Permanent hand = harness.addToBattlefieldAndReturn(player1, new HandOfVecna());
        Permanent equipped = addCreatureReady(new HillGiantHerdgorger());
        Permanent vecna = addVecna();
        hand.setAttachedTo(equipped.getId());
        harness.setHand(player1, List.of(new HillGiantHerdgorger()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, equipped)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, vecna)).isEqualTo(8);
    }

    @Test
    void manaEquipAttachesWithoutPayingLife() {
        Permanent hand = harness.addToBattlefieldAndReturn(player1, new HandOfVecna());
        Permanent creature = addCreatureReady(new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new HillGiantHerdgorger()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hand.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertLife(player1, 20);
    }

    @Test
    void lifeEquipWithEmptyHandPaysNoLife() {
        Permanent hand = harness.addToBattlefieldAndReturn(player1, new HandOfVecna());
        Permanent creature = addCreatureReady(new HillGiantHerdgorger());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hand.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertLife(player1, 20);
    }

    @Test
    void vecnaCreatedByTheBookInheritsTheCombatBoost() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        harness.addToBattlefield(player1, new EyeOfVecna());
        harness.addToBattlefield(player1, new HandOfVecna());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        Permanent vecna = findPermanent(player1, "Vecna");
        harness.setHand(player1, List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger()));
        enterBeginningOfCombat();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vecna)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, vecna)).isEqualTo(10);
    }

    @Test
    void combatBonusExpiresAtEndOfTurn() {
        Permanent hand = harness.addToBattlefieldAndReturn(player1, new HandOfVecna());
        Permanent creature = addCreatureReady(new HillGiantHerdgorger());
        hand.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new HillGiantHerdgorger()));
        enterBeginningOfCombat();
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
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
