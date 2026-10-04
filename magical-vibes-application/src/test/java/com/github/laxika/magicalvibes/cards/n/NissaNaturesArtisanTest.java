package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NissaNaturesArtisan.class, Forest.class, GrizzlyBears.class, Shock.class})
class NissaNaturesArtisanTest extends BaseCardTest {

    @Test
    void plusThreeGainsLifeAndLoyalty() {
        Permanent nissa = addReadyNissa(player1, 5);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
    }

    @Test
    void minusFourPutsAllRevealedLandsOntoBattlefieldAndRestIntoHand() {
        Permanent nissa = addReadyNissa(player1, 5);
        Card forest = new Forest();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(forest, shock));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(forest));
        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void minusTwelveBoostsOwnCreaturesWithTrampleUntilEndOfTurn() {
        Permanent nissa = addReadyNissa(player1, 12);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(ownBear.getEffectivePower()).isEqualTo(7);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(7);
        assertThat(ownBear.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opponentBear.getEffectivePower()).isEqualTo(2);
        assertThat(opponentBear.hasKeyword(Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownBear.getEffectivePower()).isEqualTo(2);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(2);
        assertThat(ownBear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new NissaNaturesArtisan());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
