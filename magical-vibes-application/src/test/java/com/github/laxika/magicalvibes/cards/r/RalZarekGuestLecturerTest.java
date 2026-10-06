package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RalZarekGuestLecturer.class, GrizzlyBears.class, HillGiant.class, ScatheZombies.class, Shock.class, Swamp.class})
class RalZarekGuestLecturerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 surveils two cards")
    void plusOneSurveilsTwo() {
        addReadyRal(player1, 3);
        Card topCard = new GrizzlyBears();
        Card secondCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("-1 makes each target player discard a card")
    void minusOneMakesEachTargetPlayerDiscard() {
        Permanent ral = addReadyRal(player1, 1);
        harness.setHand(player1, List.of(new Swamp()));
        harness.setHand(player2, List.of(new Swamp()));

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-2 returns a creature card with mana value three or less")
    void minusTwoReturnsCheapCreature() {
        Permanent ral = addReadyRal(player1, 2);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.activateAbility(player1, 0, 2, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent ->
                assertThat(permanent.getCard().getId()).isEqualTo(bears.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears);
        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-7 skips the target opponent's next turn for each head")
    void minusSevenSkipsTurnsForHeads() {
        Permanent ral = addReadyRal(player1, 7);

        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.skipNextTurnCount.getOrDefault(player2.getId(), 0)).isBetween(0, 5);
        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    void minusOneChoosesAllDiscardsBeforeRevealingAny() {
        addReadyRal(player1, 3);
        Card firstDiscard = new Swamp();
        Card secondDiscard = new Shock();
        harness.setHand(player1, List.of(firstDiscard, new GrizzlyBears()));
        harness.setHand(player2, List.of(secondDiscard, new Swamp()));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(player2.getId(), player1.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        boolean firstChoiceStayedHidden = gd.playerHands.get(player1.getId()).contains(firstDiscard)
                && !gd.playerGraveyards.get(player1.getId()).contains(firstDiscard);
        harness.handleCardChosen(player2, 0);

        assertThat(firstChoiceStayedHidden).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstDiscard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(secondDiscard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void minusOneAllowsZeroTargets() {
        Permanent ral = addReadyRal(player1, 3);
        Card card = new Swamp();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void minusOneSkipsTargetsWithEmptyHands() {
        addReadyRal(player1, 3);
        harness.setHand(player1, List.of());
        Card card = new Swamp();
        harness.setHand(player2, List.of(card));

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(card);
    }

    @Test
    void minusTwoRejectsCreatureAboveManaValueThree() {
        addReadyRal(player1, 3);
        Card giant = new HillGiant();
        harness.setGraveyard(player1, List.of(giant));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null,
                giant.getId(), Zone.GRAVEYARD)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusTwoRejectsNoncreatureCard() {
        addReadyRal(player1, 3);
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null,
                shock.getId(), Zone.GRAVEYARD)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusTwoRejectsOpponentsGraveyard() {
        addReadyRal(player1, 3);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(bears));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null,
                bears.getId(), Zone.GRAVEYARD)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusTwoDoesNotReturnTargetThatLeavesGraveyard() {
        addReadyRal(player1, 3);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.activateAbility(player1, 0, 2, null, bears.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bears));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneSatisfy(permanent ->
                assertThat(permanent.getCard().getId()).isEqualTo(bears.getId()));
    }

    @Test
    void minusSevenCannotTargetController() {
        addReadyRal(player1, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null,
                player1.getId())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusTwoReturnsCreatureAtManaValueThreeBoundary() {
        addReadyRal(player1, 3);
        Card zombies = new ScatheZombies();
        harness.setGraveyard(player1, List.of(zombies));

        harness.activateAbility(player1, 0, 2, null, zombies.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(zombies.getId());
            assertThat(permanent.isTapped()).isFalse();
            assertThat(permanent.isSummoningSick()).isTrue();
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(zombies);
    }

    @Test
    void plusOneSurveilsOnlyAvailableCard() {
        addReadyRal(player1, 3);
        Card card = new Swamp();
        harness.setLibrary(player1, List.of(card));
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
    }

    @Test
    void plusOneCanReorderBothCardsWithoutDiscarding() {
        addReadyRal(player1, 3);
        Card first = new Swamp();
        Card second = new Shock();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void minusSevenAddsExactlyOneSkippedTurnPerHead() {
        addReadyRal(player1, 7);
        gd.skipNextTurnCount.put(player2.getId(), 2);

        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        String flipLog = gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(text -> text.contains("flips 5 coins for Ral Zarek, Guest Lecturer:"))
                .findFirst().orElseThrow();
        int heads = Integer.parseInt(flipLog.replaceAll(".*: (\\d+) heads\\.", "$1"));
        assertThat(heads).isBetween(0, 5);
        assertThat(gd.skipNextTurnCount.get(player2.getId())).isEqualTo(2 + heads);
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isZero();
    }

    private Permanent addReadyRal(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new RalZarekGuestLecturer());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
