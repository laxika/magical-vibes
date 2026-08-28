package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({GarrukCursedHuntsman.class, GarrukApexPredator.class, GrizzlyBears.class,
        WrathOfGod.class, Forest.class})
class GarrukCursedHuntsmanTest extends BaseCardTest {

    @Test
    @DisplayName("0 creates two black and green Wolves")
    void zeroCreatesWolves() {
        Permanent garruk = addReadyGarruk(player1, new GarrukCursedHuntsman(), 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> wolves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Wolf"))
                .toList();

        assertThat(wolves).hasSize(2);
        assertThat(wolves).allSatisfy(wolf -> {
            assertThat(wolf.getCard().getPower()).isEqualTo(2);
            assertThat(wolf.getCard().getToughness()).isEqualTo(2);
            assertThat(wolf.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
            assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        });
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("Wolf deaths put loyalty counters on each Garruk controlled by the token's controller")
    void wolfDeathsCounterEachControlledGarruk() {
        Permanent garruk = addReadyGarruk(player1, new GarrukCursedHuntsman(), 5);
        Permanent ownOtherGarruk = addReadyGarruk(player1, new GarrukApexPredator(), 3);
        Permanent opponentGarruk = addReadyGarruk(player2, new GarrukApexPredator(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(ownOtherGarruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(opponentGarruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-3 destroys a creature and draws a card")
    void minusThreeDestroysCreatureAndDraws() {
        Permanent garruk = addReadyGarruk(player1, new GarrukCursedHuntsman(), 5);
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 cannot target a planeswalker")
    void minusThreeCannotTargetPlaneswalker() {
        Permanent garruk = addReadyGarruk(player1, new GarrukCursedHuntsman(), 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, garruk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-6 gives your creatures +3/+3 and trample")
    void minusSixCreatesCreatureBoostEmblem() {
        Permanent garruk = addReadyGarruk(player1, new GarrukCursedHuntsman(), 6);
        Permanent ownCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentCreature = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyGarruk(Player player, Card card, int loyalty) {
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        harness.addToBattlefield(player, card);
        Permanent permanent = gd.playerBattlefields.get(player.getId()).stream()
                .filter(candidate -> candidate.getCard() == card)
                .findFirst()
                .orElseThrow();
        permanent.setSummoningSick(false);
        return permanent;
    }
}
