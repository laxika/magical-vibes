package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@CardUsed({SorinGrimNemesis.class, QuilledWolf.class})
class SorinGrimNemesisTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts the revealed card into hand and makes each opponent lose its mana value")
    void plusOneRevealsCardAndEachOpponentLosesLife() {
        Permanent sorin = addReadySorin(player1, 6);
        QuilledWolf topCard = new QuilledWolf();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("-X deals X damage to a creature and gains X life")
    void minusXDealsDamageAndGainsLife() {
        Permanent sorin = addReadySorin(player1, 6);
        Permanent creature = addCreatureReady(player2, new QuilledWolf());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 1, 3, creature.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        harness.assertNotOnBattlefield(player2, "Quilled Wolf");
    }

    @Test
    @DisplayName("-9 creates one lifelink Vampire Knight for each point of the highest life total")
    void minusNineCreatesHighestLifeTotalNumberOfVampireKnights() {
        Permanent sorin = addReadySorin(player1, 9);
        harness.setLife(player1, 14);
        harness.setLife(player2, 23);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isZero();
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Vampire Knight"))
                .toList();
        assertThat(tokens).hasSize(23);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE, CardSubtype.KNIGHT);
            assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    @Test
    void plusOneWithEmptyLibraryDoesNotLoseLifeOrDraw() {
        Permanent sorin = addReadySorin(player1, 6);
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void minusXDamagesPlaneswalkerAndResolvesAfterSourceDies() {
        Permanent sorin = addReadySorin(player1, 6);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SorinGrimNemesis());
        target.setCounterCount(CounterType.LOYALTY, 8);
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 1, 6, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sorin);
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    void minusXWithZeroLeavesCreatureAndLifeUnchanged() {
        Permanent sorin = addReadySorin(player1, 6);
        Permanent target = addCreatureReady(player2, new QuilledWolf());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    void minusXDoesNotGainLifeWhenOnlyTargetLeavesBattlefield() {
        Permanent sorin = addReadySorin(player1, 6);
        Permanent target = addCreatureReady(player2, new QuilledWolf());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 1, 3, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    void minusNineUsesControllersLifeTotalAtResolution() {
        addReadySorin(player1, 10);
        harness.setLife(player1, 14);
        harness.setLife(player2, 23);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.setLife(player1, 27);
        harness.setLife(player2, 19);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Vampire Knight")).isEqualTo(27);
    }

    private Permanent addReadySorin(Player player, int loyalty) {
        Permanent sorin = harness.addToBattlefieldAndReturn(player, new SorinGrimNemesis());
        sorin.setCounterCount(CounterType.LOYALTY, loyalty);
        sorin.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return sorin;
    }
}
