package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.b.BorealGriffin;
import com.github.laxika.magicalvibes.cards.p.PanglacialWurm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MartyrOfSpores.class, BorealDruid.class, BorealGriffin.class, PanglacialWurm.class})
class MartyrOfSporesTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals green cards, gives target creature +X/+X, and sacrifices itself")
    void revealsGreenCardsAndBoostsTargetCreature() {
        Card firstGreenCard = new BorealDruid();
        Card secondGreenCard = new PanglacialWurm();
        harness.setHand(player1, List.of(firstGreenCard, secondGreenCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSpores());
        Permanent target = addCreatureReady(player1, new BorealDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, target.getId());

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstGreenCard.getId(), secondGreenCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstGreenCard.getId(), secondGreenCard.getId()));
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstGreenCard, secondGreenCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot reveal more green cards than are in hand")
    void cannotRevealMoreGreenCardsThanAreInHand() {
        harness.setHand(player1, List.of(new BorealGriffin()));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSpores());
        Permanent target = addCreatureReady(player2, new BorealDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough matching cards");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Allows revealing zero green cards and targeting an opponent's creature")
    void allowsZeroGreenCardsAndTargetsOpponentsCreature() {
        Card nonGreenCard = new BorealGriffin();
        harness.setHand(player1, List.of(nonGreenCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSpores());
        Permanent target = addCreatureReady(player2, new BorealDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonGreenCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Only green cards can be selected for the reveal cost")
    void onlyGreenCardsCanBeSelectedForRevealCost() {
        Card greenCard = new BorealDruid();
        Card nonGreenCard = new BorealGriffin();
        harness.setHand(player1, List.of(greenCard, nonGreenCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSpores());
        Permanent target = addCreatureReady(player1, new BorealDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, target.getId());

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(greenCard.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(nonGreenCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card ID");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(greenCard.getId()));
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(greenCard, nonGreenCard);
    }

    @Test
    @DisplayName("Revealing a subset fixes X even if the hand changes before resolution")
    void revealedSubsetDeterminesBoostAfterHandChanges() {
        Card firstGreenCard = new BorealDruid();
        Card secondGreenCard = new PanglacialWurm();
        harness.setHand(player1, List.of(firstGreenCard, secondGreenCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSpores());
        Permanent target = addCreatureReady(player2, new BorealDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(secondGreenCard.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstGreenCard, secondGreenCard);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Martyr can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Card greenCard = new BorealDruid();
        harness.setHand(player1, List.of(greenCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSpores());
        martyr.setSummoningSick(true);
        martyr.tap();
        Permanent target = addCreatureReady(player1, new BorealDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(greenCard.getId()));
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
    }

    @Test
    @DisplayName("The reveal selection must contain exactly X distinct cards")
    void rejectsWrongCountAndDuplicateRevealSelections() {
        Card firstGreenCard = new BorealDruid();
        Card secondGreenCard = new PanglacialWurm();
        harness.setHand(player1, List.of(firstGreenCard, secondGreenCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSpores());
        Permanent target = addCreatureReady(player1, new BorealDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, target.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(firstGreenCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(firstGreenCard.getId(), firstGreenCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(firstGreenCard.getId(), secondGreenCard.getId()));
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
    }
}
