package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.r.RhythmOfTheWild;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZhurTaaGoblin.class, RhythmOfTheWild.class})
class ZhurTaaGoblinTest extends BaseCardTest {

    @Test
    void riotAddsCounter() {
        Permanent goblin = castGoblin(true);

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isFalse();
    }

    @Test
    void riotAddsPersistentHaste() {
        Permanent goblin = castGoblin(false);

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    void riotHasteSurvivesTurnCleanup() {
        Permanent goblin = castGoblin(false);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void riotAppliesWhenEnteringWithoutBeingCast(boolean chooseCounter) {
        Permanent goblin = harness.enterBattlefieldAndReturn(player1, new ZhurTaaGoblin());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(goblin);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, chooseCounter);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(goblin);
        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(chooseCounter ? 1 : 0);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isEqualTo(!chooseCounter);
    }

    @Test
    void nativeAndGrantedRiotCanChooseCounterAndHaste() {
        harness.addToBattlefield(player1, new RhythmOfTheWild());

        Permanent goblin = castGoblin(true, false);

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    void nativeAndGrantedRiotCanEachAddCounter() {
        harness.addToBattlefield(player1, new RhythmOfTheWild());

        Permanent goblin = castGoblin(true, true);

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isFalse();
    }

    private Permanent castGoblin(boolean... counterChoices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new ZhurTaaGoblin(), "{R}{G}");
        harness.passBothPriorities();
        for (boolean chooseCounter : counterChoices) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, chooseCounter);
        }

        return findPermanent(player1, "Zhur-Taa Goblin");
    }
}
