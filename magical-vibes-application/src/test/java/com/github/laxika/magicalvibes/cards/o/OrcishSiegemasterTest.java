package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BilboUnexpectedAdventurer;
import com.github.laxika.magicalvibes.cards.g.GreatGoblinFoulHearted;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrcishSiegemaster.class, GreatGoblinFoulHearted.class,
        BilboUnexpectedAdventurer.class})
class OrcishSiegemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Other own Orcs and Goblins have trample")
    void grantsTrampleToOtherOwnOrcsAndGoblins() {
        Permanent siegemaster = addCreatureReady(player1, new OrcishSiegemaster());
        Permanent goblin = addCreatureReady(player1, new GreatGoblinFoulHearted());
        Permanent genericCreature = addCreatureReady(player1, new BilboUnexpectedAdventurer());
        Permanent opposingGoblin = addCreatureReady(player2, new GreatGoblinFoulHearted());

        assertThat(gqs.hasKeyword(gd, siegemaster, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, genericCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingGoblin, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Attacking boosts this creature by the greatest power you control")
    void boostsByGreatestControlledPower() {
        Permanent siegemaster = addCreatureReady(player1, new OrcishSiegemaster());
        addCreatureReady(player1, new GreatGoblinFoulHearted());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(siegemaster.getPowerModifier()).isEqualTo(3);
        assertThat(siegemaster.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(siegemaster.getPowerModifier()).isZero();
    }

    @Test
    void usesPowerAtResolutionAndKeepsTheResolvedBoost() {
        Permanent siegemaster = addCreatureReady(player1, new OrcishSiegemaster());
        Permanent goblin = addCreatureReady(player1, new GreatGoblinFoulHearted());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        goblin.setPlusOnePlusOneCounters(2);
        resolveAllTriggers();

        assertThat(siegemaster.getPowerModifier()).isEqualTo(5);
        assertThat(siegemaster.getToughnessModifier()).isZero();

        gd.playerBattlefields.get(player1.getId()).remove(goblin);
        assertThat(gqs.getEffectivePower(gd, siegemaster)).isEqualTo(5);
    }

    @Test
    void ignoresOpposingPowerAndIncludesItsOwnPower() {
        Permanent siegemaster = addCreatureReady(player1, new OrcishSiegemaster());
        siegemaster.setPlusOnePlusOneCounters(2);
        Permanent opponent = addCreatureReady(player2, new GreatGoblinFoulHearted());
        opponent.setPlusOnePlusOneCounters(4);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(siegemaster.getPowerModifier()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, siegemaster)).isEqualTo(4);
    }

    @Test
    void recalculatesGreatestPowerWhenStrongestCreatureLeavesBeforeResolution() {
        Permanent siegemaster = addCreatureReady(player1, new OrcishSiegemaster());
        Permanent goblin = addCreatureReady(player1, new GreatGoblinFoulHearted());
        addCreatureReady(player1, new BilboUnexpectedAdventurer());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(goblin);
        resolveAllTriggers();

        assertThat(siegemaster.getPowerModifier()).isEqualTo(2);
        assertThat(siegemaster.getToughnessModifier()).isZero();
    }

    @Test
    void grantedTrampleEndsWhenSiegemasterLeaves() {
        Permanent siegemaster = addCreatureReady(player1, new OrcishSiegemaster());
        Permanent goblin = addCreatureReady(player1, new GreatGoblinFoulHearted());
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(siegemaster);

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.TRAMPLE)).isFalse();
    }
}
