package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarkiteMarauder.class, AirElemental.class, GrizzlyBears.class, Unsummon.class})
class WarkiteMarauderTest extends BaseCardTest {

    @Test
    void attackingMakesTargetCreatureAZeroOneWithoutAbilities() {
        addCreatureReady(player1, new WarkiteMarauder());
        Permanent target = addCreatureReady(player2, new AirElemental());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(0);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    void attackTriggerCannotTargetAnAttackingPlayersCreature() {
        addCreatureReady(player1, new WarkiteMarauder());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();
    }

    @Test
    void effectExpiresAtEndOfTurn() {
        addCreatureReady(player1, new WarkiteMarauder());
        Permanent target = addCreatureReady(player2, new AirElemental());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    void countersStillModifyTheNewBaseStatsAndOtherCreaturesAreUnaffected() {
        addCreatureReady(player1, new WarkiteMarauder());
        Permanent target = addCreatureReady(player2, new WarkiteMarauder());
        Permanent other = addCreatureReady(player2, new WarkiteMarauder());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isTrue();
    }

    @Test
    void playerTwoCanTargetTheCreaturePlayerOneControlsWhenAttacking() {
        addCreatureReady(player2, new WarkiteMarauder());
        Permanent target = addCreatureReady(player1, new WarkiteMarauder());

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    void attackingWithoutALegalTargetDoesNotRequireAChoice() {
        addCreatureReady(player1, new WarkiteMarauder());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    void triggerResolvesAfterTheMarauderReturnsToHand() {
        Permanent marauder = addCreatureReady(player1, new WarkiteMarauder());
        Permanent target = addCreatureReady(player2, new WarkiteMarauder());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castInstant(player1, 0, marauder.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(marauder);
        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    void triggerDoesNotAffectAnotherCreatureWhenItsTargetReturnsToHand() {
        addCreatureReady(player1, new WarkiteMarauder());
        Permanent target = addCreatureReady(player2, new WarkiteMarauder());
        Permanent other = addCreatureReady(player2, new WarkiteMarauder());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isTrue();
    }
}
