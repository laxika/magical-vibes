package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.k.KrumarBondKin;
import com.github.laxika.magicalvibes.cards.s.SavageKnuckleblade;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({TemurCharger.class, AlpineGrizzly.class, KrumarBondKin.class, SavageKnuckleblade.class})
class TemurChargerTest extends BaseCardTest {

    @Test
    void turningFaceUpByRevealingAGreenCardGivesTargetCreatureTrampleUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());
        AlpineGrizzly greenCard = new AlpineGrizzly();
        harness.setHand(player1, List.of(new TemurCharger(), greenCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Temur Charger");
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(charger), 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(charger.isFaceDown()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void cannotTurnFaceUpWithoutRevealingAGreenCard() {
        harness.setHand(player1, List.of(new TemurCharger(), new KrumarBondKin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Temur Charger");
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(charger), 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Revealed card must be green card");
        assertThat(charger.isFaceDown()).isTrue();
    }

    @Test
    void multicolorGreenCardCanPayRevealCostAndChargerCanTargetItself() {
        SavageKnuckleblade greenCard = new SavageKnuckleblade();
        harness.setHand(player1, List.of(new TemurCharger(), greenCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Temur Charger");
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(charger), 0);

        assertThat(charger.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(greenCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(charger.getId());
        assertThat(gqs.hasKeyword(gd, charger, Keyword.TRAMPLE)).isFalse();
        harness.handlePermanentChosen(player1, charger.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, charger, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void cannotTurnFaceUpWithNoCardInHand() {
        harness.setHand(player1, List.of(new TemurCharger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Temur Charger");
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(charger)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must reveal green card");
        assertThat(charger.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingFaceUpDoesNotTriggerTrampleAbility() {
        harness.setHand(player1, List.of(new TemurCharger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Temur Charger");
        assertThat(charger.isFaceDown()).isFalse();
        assertThat(gqs.hasKeyword(gd, charger, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
