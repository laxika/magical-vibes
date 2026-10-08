package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BroodhunterWurm;
import com.github.laxika.magicalvibes.cards.k.KozileksChanneler;
import com.github.laxika.magicalvibes.cards.s.ScourFromExistence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WastelandStrangler.class, BroodhunterWurm.class, KozileksChanneler.class, ScourFromExistence.class})
class WastelandStranglerTest extends BaseCardTest {

    @Test
    void processesAnExiledCardAndGivesTargetCreatureMinusThreeMinusThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        ScourFromExistence exiledCard = new ScourFromExistence();
        harness.setExile(player2, List.of(exiledCard));

        castWastelandStrangler();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
        harness.assertInGraveyard(player2, "Scour from Existence");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    @Test
    void decliningToProcessAnExiledCardDoesNotGiveMinusThreeMinusThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        ScourFromExistence exiledCard = new ScourFromExistence();
        harness.setExile(player2, List.of(exiledCard));

        castWastelandStrangler();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
    }

    @Test
    void canProcessASingleFaceDownOpponentOwnedCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksChanneler());
        ScourFromExistence exiledCard = new ScourFromExistence();
        gd.addToExile(player2.getId(), exiledCard, null, true);

        castWastelandStrangler();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        harness.assertInGraveyard(player2, "Scour from Existence");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void noOpponentOwnedExiledCardsMeansNoShrinkEvenWithControllerOwnedExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksChanneler());
        ScourFromExistence ownExiledCard = new ScourFromExistence();
        harness.setExile(player1, List.of(ownExiledCard));

        castWastelandStrangler();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.findExiledCard(ownExiledCard.getId())).isNotNull();
    }

    @Test
    void illegalCreatureTargetPreventsProcessing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksChanneler());
        BroodhunterWurm exiledCard = new BroodhunterWurm();
        harness.setExile(player2, List.of(exiledCard));

        castWastelandStrangler();
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player2, List.of(new ScourFromExistence()));
        harness.addMana(player2, ManaColor.COLORLESS, 7);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        harness.assertNotInGraveyard(player2, "Broodhunter Wurm");
        harness.assertNotOnBattlefield(player2, "Kozilek's Channeler");
    }

    @Test
    void canTargetItsOwnCreatureAndShrinkExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KozileksChanneler());
        ScourFromExistence exiledCard = new ScourFromExistence();
        harness.setExile(player2, List.of(exiledCard));

        castWastelandStrangler();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Scour from Existence");

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UNTAP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void canProcessExactlyOneOfSeveralOpponentOwnedCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksChanneler());
        ScourFromExistence chosenCard = new ScourFromExistence();
        BroodhunterWurm otherCard = new BroodhunterWurm();
        ScourFromExistence ownCard = new ScourFromExistence();
        harness.setExile(player2, List.of(chosenCard, otherCard));
        harness.setExile(player1, List.of(ownCard));

        castWastelandStrangler();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosenCard.getId()));

        harness.assertInGraveyard(player2, "Scour from Existence");
        assertThat(gd.findExiledCard(chosenCard.getId())).isNull();
        assertThat(gd.findExiledCard(otherCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(ownCard.getId())).isNotNull();
        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
    }

    private void castWastelandStrangler() {
        harness.castFromHand(player1, new WastelandStrangler(), "{2}{B}");
        harness.passBothPriorities();
    }
}
