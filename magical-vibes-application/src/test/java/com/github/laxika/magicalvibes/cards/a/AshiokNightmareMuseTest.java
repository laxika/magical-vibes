package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({AshiokNightmareMuse.class, Forest.class, GrizzlyBears.class})
class AshiokNightmareMuseTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates a Nightmare whose attack trigger exiles the top two cards of each opponent's library")
    void plusOneCreatesNightmareWithAttackTrigger() {
        Permanent ashiok = addReadyAshiok(player1, 3);
        Card opponentFirst = new Forest();
        Card opponentSecond = new GrizzlyBears();
        Card ownTop = new Forest();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opponentFirst, opponentSecond));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent nightmare = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(3);

        nightmare.setSummoningSick(false);
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(nightmare)));
        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(opponentFirst, opponentSecond);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-3 returns a nonland permanent and its owner exiles a card from hand")
    void minusThreeBouncesThenExilesFromOwnerHand() {
        Permanent ashiok = addReadyAshiok(player1, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card handCard = new Forest();
        harness.setHand(player2, List.of(handCard));

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ExileFromHandChoice.class))
                .isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isZero();
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(handCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(handCard);
    }

    @Test
    @DisplayName("-7 offers up to three face-up spells owned by opponents from exile")
    void minusSevenCastsAtMostThreeOpponentOwnedFaceUpSpells() {
        Permanent ashiok = addReadyAshiok(player1, 7);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        Card opponentLand = new Forest();
        Card ownSpell = new GrizzlyBears();
        Card faceDown = new GrizzlyBears();
        gd.addToExile(player2.getId(), first);
        gd.addToExile(player2.getId(), second);
        gd.addToExile(player2.getId(), third);
        gd.addToExile(player2.getId(), fourth);
        gd.addToExile(player2.getId(), opponentLand);
        gd.addToExile(player1.getId(), ownSpell);
        gd.addToExile(player2.getId(), faceDown, null, true);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactly(
                first.getId(), second.getId(), third.getId(), fourth.getId());
        assertThat(choice.maxCount()).isEqualTo(3);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(first.getId())
                        || permanent.getCard().getId().equals(second.getId())
                        || permanent.getCard().getId().equals(third.getId())))
                .hasSize(3);
        assertThat(gd.findExiledCard(fourth.getId())).isNotNull();
        assertThat(gd.findExiledCard(opponentLand.getId())).isNotNull();
        assertThat(gd.findExiledCard(ownSpell.getId())).isNotNull();
        assertThat(gd.findExiledCard(faceDown.getId())).isNotNull();
        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-3 cannot target a land")
    void minusThreeCannotTargetLand() {
        addReadyAshiok(player1, 3);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAshiok(Player player, int loyalty) {
        Permanent perm = new Permanent(new AshiokNightmareMuse());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
