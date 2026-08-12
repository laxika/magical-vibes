package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SorinSolemnVisitorTest extends BaseCardTest {

    @Test
    @DisplayName("+1 boosts your creatures and grants lifelink until your next turn")
    void plusOneBoostsOwnCreaturesUntilNextTurn() {
        Permanent sorin = addReadySorin(player1, 3);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.LIFELINK)).isFalse();

        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        ownCreature.clearUntilNextTurnEffects();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("-2 creates a 2/2 black Vampire creature token with flying")
    void minusTwoCreatesFlyingVampireToken() {
        Permanent sorin = addReadySorin(player1, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Vampire");
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("-6 creates an emblem that triggers during each opponent's upkeep")
    void minusSixCreatesOpponentUpkeepEmblem() {
        Permanent sorin = addReadySorin(player1, 6);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Grizzly Bears")).isZero();
    }

    private Permanent addReadySorin(Player player, int loyalty) {
        Permanent sorin = new Permanent(new SorinSolemnVisitor());
        sorin.setCounterCount(CounterType.LOYALTY, loyalty);
        sorin.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sorin);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return sorin;
    }
}
