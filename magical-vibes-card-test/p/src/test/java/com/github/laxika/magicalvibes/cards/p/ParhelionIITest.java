package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParhelionII.class, ParadiseDruid.class})
class ParhelionIITest extends BaseCardTest {

    @Test
    void attackingCreatesTwoUntappedAttackingAngels() {
        addCreatureReady(player1, new ParhelionII());
        addCreatureReady(player1, new ParadiseDruid());
        addCreatureReady(player1, new ParadiseDruid());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        List<Permanent> angels = findPermanents(player1, "Angel");
        assertThat(angels).hasSize(2)
                .allSatisfy(angel -> {
                    assertThat(angel.isAttacking()).isTrue();
                    assertThat(angel.isTapped()).isFalse();
                    assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
                    assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
                });
    }

    @Test
    void crewFourAnimatesParhelionAndTapsCreaturesWithTotalPowerFour() {
        Permanent parhelion = addCreatureReady(player1, new ParhelionII());
        Permanent firstCrew = addCreatureReady(player1, new ParadiseDruid());
        Permanent secondCrew = addCreatureReady(player1, new ParadiseDruid());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, parhelion)).isTrue();
        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
    }

    @Test
    void insufficientCrewPowerCannotActivate() {
        Permanent parhelion = addCreatureReady(player1, new ParhelionII());
        Permanent crew = addCreatureReady(player1, new ParadiseDruid());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, parhelion)).isFalse();
        assertThat(crew.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }

    @Test
    void summoningSickCreaturesCanCrewAndAnimationExpiresAtCleanup() {
        Permanent parhelion = addCreatureReady(player1, new ParhelionII());
        Permanent firstCrew = harness.addToBattlefieldAndReturn(player1, new ParadiseDruid());
        Permanent secondCrew = harness.addToBattlefieldAndReturn(player1, new ParadiseDruid());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, parhelion)).isTrue();
        assertThat(findPermanents(player1, "Angel")).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, parhelion)).isFalse();
    }

    @Test
    void attackTriggerResolvesAfterParhelionLeavesBattlefield() {
        Permanent parhelion = addCreatureReady(player1, new ParhelionII());
        addCreatureReady(player1, new ParadiseDruid());
        addCreatureReady(player1, new ParadiseDruid());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        gd.playerBattlefields.get(player1.getId()).remove(parhelion);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);

        assertThat(findPermanents(player1, "Angel")).hasSize(2).allSatisfy(angel -> {
            assertThat(angel.isAttacking()).isTrue();
            assertThat(angel.isTapped()).isFalse();
            assertThat(angel.getAttackTarget()).isEqualTo(player2.getId());
            assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
        });
    }
}
