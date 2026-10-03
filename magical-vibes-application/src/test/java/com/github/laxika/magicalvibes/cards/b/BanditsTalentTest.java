package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ContagionClasp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({BanditsTalent.class, Forest.class, GrizzlyBears.class, ContagionClasp.class})
class BanditsTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent may discard a nonland card instead of two cards when it enters")
    void entersOffersNonlandAlternative() {
        Card nonland = new GrizzlyBears();
        harness.setHand(player1, List.of(new BanditsTalent()));
        harness.setHand(player2, List.of(nonland, new Forest()));
        castTalent();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(nonland);
    }

    @Test
    @DisplayName("An opponent without a nonland card discards two cards when it enters")
    void entersForcesTwoCardDiscardWithoutNonland() {
        harness.setHand(player1, List.of(new BanditsTalent()));
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        castTalent();

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("At level 2, an opponent with one or fewer cards in hand loses 2 life on their upkeep")
    void levelTwoPunishesSmallOpponentHand() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new BanditsTalent());
        levelUp(talent, 0, 0);
        harness.setHand(player2, List.of(new Forest()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("At level 3, draws one additional card for the opponent with one card in hand")
    void levelThreeDrawsForSmallOpponentHands() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new BanditsTalent());
        levelUp(talent, 0, 0);
        levelUp(talent, 1, 3);
        drawWithOpponentHand(List.of(new Forest()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void levelOneDoesNotPunishSmallOpponentHand() {
        harness.addToBattlefield(player1, new BanditsTalent());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void levelTwoDoesNotPunishOpponentWithTwoCards() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new BanditsTalent());
        levelUp(talent, 0, 0);
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void upkeepRechecksOpponentHandOnResolution() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new BanditsTalent());
        levelUp(talent, 0, 0);
        harness.setHand(player2, List.of(new Forest()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void opponentCanDeclineNonlandAlternativeAndDiscardTwo() {
        harness.setHand(player1, List.of(new BanditsTalent()));
        harness.setHand(player2, List.of(new BanditsTalent(), new Forest(), new Forest()));
        castTalent();

        harness.handleMayAbilityChosen(player2, false);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void opponentWithOnlyOneLandDiscardsIt() {
        harness.setHand(player1, List.of(new BanditsTalent()));
        harness.setHand(player2, List.of(new Forest()));
        castTalent();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void emptyOpponentHandDoesNotRequireDiscardChoice() {
        harness.setHand(player1, List.of(new BanditsTalent()));
        harness.setHand(player2, List.of());
        castTalent();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void levelTwoDoesNotDrawAdditionalCard() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new BanditsTalent());
        levelUp(talent, 0, 0);
        drawWithOpponentHand(List.of());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void levelThreeDrawsAdditionalCardForEmptyOpponentHand() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new BanditsTalent());
        levelUp(talent, 0, 0);
        levelUp(talent, 1, 3);
        drawWithOpponentHand(List.of());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void levelThreeDoesNotDrawAdditionalCardForTwoCardOpponentHand() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new BanditsTalent());
        levelUp(talent, 0, 0);
        levelUp(talent, 1, 3);
        drawWithOpponentHand(List.of(new Forest(), new Forest()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void classLevelDoesNotMakeTalentEligibleForProliferate() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new BanditsTalent());
        levelUp(talent, 0, 0);
        harness.addToBattlefield(player1, new ContagionClasp());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void levelTwoHasNoDrawStepTrigger() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new BanditsTalent());
        levelUp(talent, 0, 0);
        beginDrawWithOpponentHand(List.of());

        assertThat(gd.stack).isEmpty();
    }
    private void drawWithOpponentHand(List<Card> opponentHand) {
        beginDrawWithOpponentHand(opponentHand);
        resolveAllTriggers();
    }

    private void beginDrawWithOpponentHand(List<Card> opponentHand) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, opponentHand);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        gd.turnNumber = 2;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.DRAW);
    }

    private void castTalent() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
    }

    private void levelUp(Permanent talent, int abilityIndex, int genericMana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        int talentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(talent);
        harness.activateAbility(player1, talentIndex, abilityIndex, null, null);
        harness.passBothPriorities();
    }
}
