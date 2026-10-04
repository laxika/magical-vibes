package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlimmerBairn.class})
class GlimmerBairnTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a token gives Glimmer Bairn +2/+2 until end of turn")
    void sacrificingTokenBoostsGlimmerBairn() {
        Permanent bairn = addCreatureReady(player1, new GlimmerBairn());
        harness.addToBattlefield(player1, createToken());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bairn)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bairn)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Glimmer Bairn cannot activate without a token to sacrifice")
    void cannotActivateWithoutToken() {
        addCreatureReady(player1, new GlimmerBairn());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Glimmer Bairn's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bairn = addCreatureReady(player1, new GlimmerBairn());
        harness.addToBattlefield(player1, createToken());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bairn)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bairn)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bairn)).isEqualTo(2);
    }

    @Test
    @DisplayName("A noncreature token can pay the cost and is sacrificed before resolution")
    void noncreatureTokenIsSacrificedAsCost() {
        Permanent bairn = addCreatureReady(player1, new GlimmerBairn());
        Card treasure = new Card();
        treasure.setName("Treasure");
        treasure.setType(CardType.ARTIFACT);
        treasure.setToken(true);
        harness.addToBattlefield(player1, treasure);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(bairn);
        assertThat(gqs.getEffectivePower(gd, bairn)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bairn)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bairn)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's token cannot pay Glimmer Bairn's sacrifice cost")
    void cannotSacrificeOpponentsToken() {
        addCreatureReady(player1, new GlimmerBairn());
        harness.addToBattlefield(player2, createToken());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Glimmer Bairn can activate repeatedly")
    void canActivateWhileTappedAndSummoningSickAndBoostsStack() {
        harness.addToBattlefield(player1, new GlimmerBairn());
        Permanent bairn = findPermanent(player1, "Glimmer Bairn");
        bairn.setSummoningSick(true);
        bairn.setTapped(true);
        harness.addToBattlefield(player1, createToken());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, createToken());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bairn)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bairn)).isEqualTo(6);
        assertThat(bairn.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(bairn);
    }

    private Card createToken() {
        Card token = new Card();
        token.setName("Saproling");
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.GREEN);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
