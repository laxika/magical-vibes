package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WanderingMusicians.class})
class WanderingMusiciansTest extends BaseCardTest {

    @Test
    void attackingBoostsAllCreaturesYouControl() {
        Permanent musicians = addCreatureReady(player1, new WanderingMusicians());
        Permanent otherCreature = addCreatureReady(player1, new WanderingMusicians());
        Permanent opposingCreature = addCreatureReady(player2, new WanderingMusicians());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(musicians.getPowerModifier()).isEqualTo(1);
        assertThat(otherCreature.getPowerModifier()).isEqualTo(1);
        assertThat(opposingCreature.getPowerModifier()).isZero();
        assertThat(musicians.getToughnessModifier()).isZero();
        assertThat(otherCreature.getToughnessModifier()).isZero();
    }

    @Test
    void attackBonusExpiresAtEndOfTurn() {
        Permanent musicians = addCreatureReady(player1, new WanderingMusicians());
        Permanent otherCreature = addCreatureReady(player1, new WanderingMusicians());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(musicians.getPowerModifier()).isEqualTo(1);
        assertThat(otherCreature.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(musicians.getPowerModifier()).isZero();
        assertThat(otherCreature.getPowerModifier()).isZero();
    }

    @Test
    void eachAttackingMusicianAddsItsOwnBonus() {
        Permanent first = addCreatureReady(player1, new WanderingMusicians());
        Permanent second = addCreatureReady(player1, new WanderingMusicians());
        Permanent nonattacker = addCreatureReady(player1, new WanderingMusicians());
        Permanent opponent = addCreatureReady(player2, new WanderingMusicians());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            resolveAllTriggers();
        });

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(nonattacker.getPowerModifier()).isEqualTo(2);
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
    }

    @Test
    void creaturesEnteringBeforeResolutionReceiveBonusButLaterCreaturesDoNot() {
        Permanent musicians = addCreatureReady(player1, new WanderingMusicians());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new WanderingMusicians());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new WanderingMusicians());

        assertThat(musicians.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getPowerModifier()).isEqualTo(1);
        assertThat(afterResolution.getPowerModifier()).isZero();
    }

    @Test
    void attackTriggerResolvesAfterMusiciansLeaveBattlefield() {
        Permanent musicians = addCreatureReady(player1, new WanderingMusicians());
        Permanent otherCreature = addCreatureReady(player1, new WanderingMusicians());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(musicians);
        harness.setGraveyard(player1, List.of(musicians.getCard()));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(otherCreature.getPowerModifier()).isEqualTo(1);
        assertThat(otherCreature.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
