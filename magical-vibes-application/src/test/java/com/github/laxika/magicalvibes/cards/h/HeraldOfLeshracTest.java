package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldOfLeshrac.class, SnowCoveredIsland.class})
class HeraldOfLeshracTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each land controlled but not owned")
    void boostsForControlledLandsNotOwned() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfLeshrac());
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        Permanent stolenLand = harness.addToBattlefieldAndReturn(player1, new SnowCoveredIsland());
        gd.stolenCreatures.put(stolenLand.getId(), player2.getId());

        assertThat(gqs.getEffectivePower(gd, herald)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, herald)).isEqualTo(5);
    }

    @Test
    @DisplayName("Paying cumulative upkeep gains control of an opponent's land")
    void paysCumulativeUpkeepByGainingLand() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfLeshrac());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(herald.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(herald, island);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(island);
    }

    @Test
    @DisplayName("Is sacrificed when its cumulative upkeep cannot gain control of a land")
    void isSacrificedWhenNoLandCanBeGained() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfLeshrac());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(herald);
        harness.assertInGraveyard(player1, "Herald of Leshrac");
    }

    @Test
    @DisplayName("Second upkeep gains two distinct lands and updates the boost")
    void secondUpkeepRequiresTwoDistinctLands() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfLeshrac());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, first.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());

        assertThat(herald.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(herald, first, second, third);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(unchosen);
        assertThat(gqs.getEffectivePower(gd, herald)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, herald)).isEqualTo(7);
    }

    @Test
    @DisplayName("Insufficient lands for the entire upkeep causes sacrifice without partial payment")
    void cannotPartiallyPaySecondUpkeep() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfLeshrac());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, first.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Herald of Leshrac");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(herald, remaining);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(remaining).doesNotContain(first);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, remaining);
    }

    @Test
    @DisplayName("Upkeep may be declined even when a land is available")
    void canDeclineAffordableUpkeep() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfLeshrac());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Herald of Leshrac");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(herald, island);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(island);
    }

    @Test
    @DisplayName("When Herald of Leshrac leaves, only lands it controlled return to their owners")
    void returnsOnlyControlledOwnedLandsWhenItLeaves() {
        harness.addToBattlefield(player1, new HeraldOfLeshrac());
        Permanent landOwnedByPlayer2 = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        Permanent landOwnedByPlayer1 = harness.addToBattlefieldAndReturn(player1, new SnowCoveredIsland());
        CreatureControlService control = GameTestEngineContext.get().getBean(CreatureControlService.class);
        harness.inMutationScope(() -> {
            control.applyControlEffect(gd, player1.getId(), landOwnedByPlayer2,
                    new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT, null, "Test");
            control.applyControlEffect(gd, player2.getId(), landOwnedByPlayer1,
                    new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT, null, "Test");
        });

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(landOwnedByPlayer2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(landOwnedByPlayer1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(landOwnedByPlayer2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(landOwnedByPlayer1);
    }
}
