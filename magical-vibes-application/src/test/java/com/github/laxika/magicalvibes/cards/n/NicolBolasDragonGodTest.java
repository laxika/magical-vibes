package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.u.UginTheIneffable;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({NicolBolasDragonGod.class, UginTheIneffable.class, GarrukWildspeaker.class,
        GrizzlyBears.class, Forest.class})
class NicolBolasDragonGodTest extends BaseCardTest {

    @Test
    @DisplayName("+1 draws before the opponent chooses a permanent or hand card to exile")
    void plusOneDrawsAndExilesOpponentChoice() {
        Card drawnCard = new GrizzlyBears();
        Card opponentHandCard = new GrizzlyBears();
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("-3 destroys a target creature")
    void minusThreeDestroysCreature() {
        Permanent nicol = addReadyNicol(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        addReadyPlaneswalker(player2, new GarrukWildspeaker(), 3);

        harness.activateAbility(player1, battlefieldIndex(player1, nicol), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.winnerPlayerId).isNull();
    }

    private Permanent addReadyNicol(Player player, int loyalty) {
        return addReadyPlaneswalker(player, new NicolBolasDragonGod(), loyalty);
    }

    private Permanent addReadyPlaneswalker(Player player, Card card, int loyalty) {
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
