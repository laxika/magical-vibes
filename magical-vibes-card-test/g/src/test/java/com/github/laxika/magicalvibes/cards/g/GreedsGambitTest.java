package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.z.ZulaportCutthroat;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({GreedsGambit.class, GrizzlyBears.class, ZulaportCutthroat.class})
class GreedsGambitTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws three, gains 6 life, and creates three flying Bats")
    void entersWithItsReward() {
        harness.setHand(player1, List.of(new GreedsGambit()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> "Bat".equals(p.getCard().getName()))
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Controller's end step discards, loses life, and sacrifices a creature")
    void endStepDownsideResolvesInOrder() {
        harness.addToBattlefield(player1, new GreedsGambit());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        moveToEndStep();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, sacrificed.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Leaves-the-battlefield trigger discards three, loses 6 life, and sacrifices three creatures")
    void leavesBattlefieldDownsideResolves() {
        Permanent gambit = harness.addToBattlefieldAndReturn(player1, new GreedsGambit());
        List<Permanent> creatures = List.of(
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, gambit));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(3);
        harness.handleMultiplePermanentsChosen(player1,
                creatures.subList(0, 3).stream().map(Permanent::getId).toList());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count()).isEqualTo(6);
    }

    @Test
    @DisplayName("The reward creates black 2/1 Bat tokens with flying for its controller")
    void rewardCreatesTheSpecifiedTokens() {
        harness.setHand(player1, List.of(new GreedsGambit()));
        harness.setLibrary(player1, List.of(new GreedsGambit(), new GreedsGambit(), new GreedsGambit()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).hasSize(3).allSatisfy(bat -> {
            assertThat(bat.getCard().isToken()).isTrue();
            assertThat(bat.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(bat.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(bat.getCard().getSubtypes()).contains(CardSubtype.BAT);
            assertThat(gqs.getEffectivePower(gd, bat)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, bat)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, bat, Keyword.FLYING)).isTrue();
        });
        assertThat(countPermanents(player2, "Bat")).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The end step penalty loses life even with no cards or creatures")
    void endStepPenaltyWithEmptyResources() {
        harness.addToBattlefield(player1, new GreedsGambit());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        moveToEndStep();
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Greed's Gambit");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The penalty does not trigger at the opponent's end step")
    void opponentEndStepDoesNotTriggerPenalty() {
        harness.addToBattlefield(player1, new GreedsGambit());
        harness.setHand(player1, List.of(new GreedsGambit()));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Leaving still loses 6 life with no cards or creatures")
    void leavingWithEmptyResources() {
        Permanent gambit = harness.addToBattlefieldAndReturn(player1, new GreedsGambit());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, gambit));
        resolveAllTriggers();

        harness.assertLife(player1, 14);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Leaving discards and sacrifices as much as possible when fewer than three exist")
    void leavingWithInsufficientResources() {
        Permanent gambit = harness.addToBattlefieldAndReturn(player1, new GreedsGambit());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GreedsGambit(), new GreedsGambit()));
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, gambit));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 14);
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("All three sacrificed creatures see each other's simultaneous deaths")
    void leavingSacrificesThreeCreaturesSimultaneously() {
        Permanent gambit = harness.addToBattlefieldAndReturn(player1, new GreedsGambit());
        harness.addToBattlefield(player1, new ZulaportCutthroat());
        harness.addToBattlefield(player1, new ZulaportCutthroat());
        harness.addToBattlefield(player1, new ZulaportCutthroat());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, gambit));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Zulaport Cutthroat")).isZero();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 11);
    }

    private void moveToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
