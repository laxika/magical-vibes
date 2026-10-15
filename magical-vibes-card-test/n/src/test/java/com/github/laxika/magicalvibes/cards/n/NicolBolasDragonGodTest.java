package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.f.FblthpTheLost;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.u.UginTheIneffable;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NicolBolasDragonGod.class, UginTheIneffable.class,
        PrimordialWurm.class, FblthpTheLost.class, Forest.class})
class NicolBolasDragonGodTest extends BaseCardTest {

    @Test
    @DisplayName("+1 draws before the opponent chooses a permanent or hand card to exile")
    void plusOneDrawsAndExilesOpponentChoice() {
        Card drawnCard = new PrimordialWurm();
        Card opponentHandCard = new PrimordialWurm();
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player2, List.of(opponentHandCard));
        Permanent nicol = addReadyNicol(player1, 4);

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExilePermanentsOrHandCardsChoice.class);

        harness.handleMultipleCardsChosen(player2, List.of(opponentPermanent.getCard().getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentHandCard);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(opponentPermanent.getCard().getId());
    }

    @Test
    @DisplayName("Gains the loyalty abilities of another planeswalker")
    void gainsOtherPlaneswalkerLoyaltyAbilities() {
        Permanent nicol = addReadyNicol(player1, 4);
        addReadyPlaneswalker(player2, new UginTheIneffable(), 3);
        harness.setLibrary(player1, List.of(new PrimordialWurm()));

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("-3 destroys a target creature")
    void minusThreeDestroysCreature() {
        Permanent nicol = addReadyNicol(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("-3 accepts a target planeswalker")
    void minusThreeDestroysPlaneswalker() {
        Permanent nicol = addReadyNicol(player1, 4);
        Permanent target = addReadyPlaneswalker(player2, new UginTheIneffable(), 3);

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("-3 rejects a noncreature, nonplaneswalker target")
    void minusThreeRejectsInvalidTarget() {
        Permanent nicol = addReadyNicol(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, nicol), 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-8 makes an opponent without a legendary creature or planeswalker lose")
    void minusEightMakesUnprotectedOpponentLose() {
        Permanent nicol = addReadyNicol(player1, 9);

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("-8 does not make an opponent with a planeswalker lose")
    void minusEightSpareOpponentWithPlaneswalker() {
        Permanent nicol = addReadyNicol(player1, 9);
        addReadyPlaneswalker(player2, new UginTheIneffable(), 3);

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    @DisplayName("-8 also spares an opponent with a nonlegendary planeswalker")
    void minusEightSpareOpponentWithNonlegendaryPlaneswalker() {
        Permanent nicol = addReadyNicol(player1, 9);
        Permanent planeswalker = addReadyPlaneswalker(player2, new UginTheIneffable(), 3);
        planeswalker.getPersistentRemovedSupertypes().add(CardSupertype.LEGENDARY);

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    void plusOneCanExileAHandCardInsteadOfAPermanent() {
        Permanent nicol = addReadyNicol(player1, 4);
        Card drawn = new PrimordialWurm();
        Card handCard = new PrimordialWurm();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(handCard));

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(handCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(nicol.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void plusOneStillDrawsWhenOpponentHasNothingToExile() {
        Permanent nicol = addReadyNicol(player1, 4);
        Card drawn = new PrimordialWurm();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nicol);
    }

    @Test
    void cannotActivateAnAbilityAnotherPlaneswalkerHasLost() {
        Permanent nicol = addReadyNicol(player1, 4);
        Permanent ugin = addReadyPlaneswalker(player2, new UginTheIneffable(), 4);
        ugin.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setLibrary(player1, List.of(new PrimordialWurm()));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, nicol), 3, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(nicol.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void losesBorrowedAbilitiesWhenTheOtherPlaneswalkerLeaves() {
        Permanent nicol = addReadyNicol(player1, 4);
        Permanent ugin = addReadyPlaneswalker(player2, new UginTheIneffable(), 4);
        gd.playerBattlefields.get(player2.getId()).remove(ugin);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, nicol), 3, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void borrowedAndOwnAbilitiesShareTheOncePerTurnLimit() {
        Permanent nicol = addReadyNicol(player1, 4);
        addReadyPlaneswalker(player2, new UginTheIneffable(), 4);
        harness.setLibrary(player1, List.of(new PrimordialWurm()));

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 3, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, nicol), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(nicol.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void minusThreeResolvesWhenPayingItsCostRemovesNicol() {
        Permanent nicol = addReadyNicol(player1, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nicol.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void minusEightSparesAnOpponentWithALegendaryCreature() {
        Permanent nicol = addReadyNicol(player1, 8);
        harness.addToBattlefield(player2, new FblthpTheLost());

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.winnerPlayerId).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nicol.getCard());
    }

    @Test
    void minusEightDoesNotSpareAnOpponentWithOnlyANonlegendaryCreature() {
        Permanent nicol = addReadyNicol(player1, 8);
        harness.addToBattlefield(player2, new PrimordialWurm());

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    private Permanent addReadyNicol(Player player, int loyalty) {
        return addReadyPlaneswalker(player, new NicolBolasDragonGod(), loyalty);
    }

    private Permanent addReadyPlaneswalker(Player player, Card card, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
