package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreasureNabber.class, SolRing.class, Forest.class})
class TreasureNabberTest extends BaseCardTest {

    @Test
    void gainsControlOfAnOpponentsArtifactTappedForManaUntilEndOfYourNextTurn() {
        harness.addToBattlefield(player1, new TreasureNabber());
        Permanent solRing = harness.addToBattlefieldAndReturn(player2, new SolRing());

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getId().equals(solRing.getId()));
        assertThat(gd.newestControlEffectFor(solRing.getId()).duration())
                .isEqualTo(com.github.laxika.magicalvibes.model.effect.EffectDuration.UNTIL_END_OF_YOUR_NEXT_TURN);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getId().equals(solRing.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getId().equals(solRing.getId()));
    }

    @Test
    void doesNotTriggerForAnOpponentsNonartifactManaPermanent() {
        harness.addToBattlefield(player1, new TreasureNabber());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.newestControlEffectFor(forest.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getId().equals(forest.getId()));
    }
}
