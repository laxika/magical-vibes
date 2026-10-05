package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.cards.b.BloodPrice;
import com.github.laxika.magicalvibes.cards.n.NimanaSkydancer;
import com.github.laxika.magicalvibes.cards.s.ScionOfTheSwarm;
import com.github.laxika.magicalvibes.cards.s.SpareSupplies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InscriptionOfRuin.class, CliffhavenSellSword.class, BloodPrice.class,
        NimanaSkydancer.class, ScionOfTheSwarm.class, SpareSupplies.class})
class InscriptionOfRuinTest extends BaseCardTest {

    @Test
    void opponentDiscardsTwoCards() {
        harness.setHand(player2, new ArrayList<>(List.of(new CliffhavenSellSword(), new CliffhavenSellSword())));
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0}, List.of(player2.getId()), List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void returnsTargetCreatureWithManaValueTwoOrLess() {
        Card creature = new CliffhavenSellSword();
        harness.setGraveyard(player1, List.of(creature));
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1}, List.of(creature.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Inscription of Ruin");
    }

    @Test
    void destroysTargetCreatureWithManaValueThreeOrLess() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{2}, List.of(creature.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Cliffhaven Sell-Sword");
    }

    @Test
    void kickerAllowsChoosingAllThreeModesInOrder() {
        Card reanimated = new CliffhavenSellSword();
        harness.setGraveyard(player1, List.of(reanimated));
        harness.setHand(player2, new ArrayList<>(List.of(new CliffhavenSellSword(), new CliffhavenSellSword())));
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new InscriptionOfRuin()));
        addMana(7);

        gs.playCard(gd, player1, 0, ChooseOneEffect.encodeModeSelection(1, 3, new int[]{0, 1, 2}),
                null, null, List.of(player2.getId(), reanimated.getId(), destroyed.getId()),
                List.of(), false, null, null, null, null, null, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(reanimated.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(destroyed);
    }

    @Test
    void cannotChooseMultipleModesWithoutKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        prepareCard();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{0, 2}, List.of(player2.getId(), target.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedSpellCanChooseNoModes() {
        harness.setHand(player1, List.of(new InscriptionOfRuin()));
        addMana(7);

        gs.playCard(gd, player1, 0, ChooseOneEffect.encodeModeSelection(0, 3, new int[]{}),
                null, null, List.of(), List.of(), false, null, null, null, null, null, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Inscription of Ruin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void discardModeCannotTargetController() {
        prepareCard();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{0}, List.of(player1.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentWithOneCardDiscardsOnlyThatCard() {
        harness.setHand(player2, List.of(new CliffhavenSellSword()));
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0}, List.of(player2.getId()), List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Cliffhaven Sell-Sword");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void discardModeResolvesAgainstEmptyHand() {
        harness.setHand(player2, List.of());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0}, List.of(player2.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Inscription of Ruin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotReturnCreatureWithManaValueThree() {
        Card creature = new NimanaSkydancer();
        harness.setGraveyard(player1, List.of(creature));
        prepareCard();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{1}, List.of(creature.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReturnNoncreatureCard() {
        Card spell = new SpareSupplies();
        harness.setGraveyard(player1, List.of(spell));
        prepareCard();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{1}, List.of(spell.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReturnCreatureFromOpponentsGraveyard() {
        Card creature = new CliffhavenSellSword();
        harness.setGraveyard(player2, List.of(creature));
        prepareCard();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{1}, List.of(creature.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysCreatureWithManaValueExactlyThree() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NimanaSkydancer());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{2}, List.of(creature.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Nimana Skydancer");
    }

    @Test
    void cannotDestroyCreatureWithManaValueGreaterThanThree() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScionOfTheSwarm());
        prepareCard();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{2}, List.of(creature.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroyModeCanTargetControllersCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{2}, List.of(creature.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Cliffhaven Sell-Sword");
    }

    @Test
    void kickedSpellStillDestroysCreatureWhenGraveyardTargetDisappears() {
        Card returned = new CliffhavenSellSword();
        harness.setGraveyard(player1, List.of(returned));
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new NimanaSkydancer());
        harness.setHand(player1, List.of(new InscriptionOfRuin()));
        addMana(7);

        gs.playCard(gd, player1, 0, ChooseOneEffect.encodeModeSelection(1, 3, new int[]{1, 2}),
                null, null, List.of(returned.getId(), destroyed.getId()),
                List.of(), false, null, null, null, null, null, true);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(destroyed);
        harness.assertInGraveyard(player2, "Nimana Skydancer");
    }

    @Test
    void opponentChoosesWhichTwoCardsToDiscard() {
        Card kept = new CliffhavenSellSword();
        Card firstDiscard = new BloodPrice();
        Card secondDiscard = new NimanaSkydancer();
        harness.setHand(player2, List.of(kept, firstDiscard, secondDiscard));
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0}, List.of(player2.getId()), List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(firstDiscard, secondDiscard);
    }

    @Test
    void cannotDestroyNoncreaturePermanentWithLowManaValue() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SpareSupplies());
        prepareCard();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{2}, List.of(artifact.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void graveyardTargetMustStillBePresentWhenSpellResolves() {
        Card creature = new CliffhavenSellSword();
        harness.setGraveyard(player1, List.of(creature));
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1}, List.of(creature.getId()), List.of());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Inscription of Ruin");
    }

    @Test
    void kickedSpellCanChooseOnlyOneMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new InscriptionOfRuin()));
        addMana(7);

        gs.playCard(gd, player1, 0, ChooseOneEffect.encodeModeSelection(1, 3, new int[]{2}),
                null, null, List.of(creature.getId()), List.of(), false, null, null, null, null, null, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Cliffhaven Sell-Sword");
    }

    @Test
    void kickerRequiresPayingItsAdditionalManaCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        prepareCard();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 3, new int[]{0, 2}),
                null, null, List.of(player2.getId(), creature.getId()),
                List.of(), false, null, null, null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new InscriptionOfRuin()));
        addMana(3);
    }

    private void addMana(int amount) {
        harness.addMana(player1, ManaColor.BLACK, amount);
    }
}
