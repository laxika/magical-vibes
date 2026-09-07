package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IllusionaryTerrain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GorillaPack.class, Forest.class, IllusionaryTerrain.class})
class GorillaPackTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed when controller controls no Forests")
    void sacrificedWhenNoForests() {
        harness.castFromHand(player1, new GorillaPack(), "{2}{G}");
        harness.passBothPriorities(); // resolve creature → state trigger fires
        harness.passBothPriorities(); // resolve state trigger → sacrificed

        harness.assertNotOnBattlefield(player1, "Gorilla Pack");
        harness.assertInGraveyard(player1, "Gorilla Pack");
    }

    @Test
    @DisplayName("Survives while controller controls a Forest")
    void survivesWithForest() {
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new GorillaPack(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gorilla Pack");
    }

    @Test
    @DisplayName("Can attack when defending player controls a Forest")
    void canAttackWhenDefenderControlsForest() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Forest()); // keep the pack alive
        harness.addToBattlefield(player2, new Forest());

        Permanent pack = addCreatureReady(player1, new GorillaPack());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(pack)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Forest")
    void cannotAttackWhenDefenderHasNoForest() {
        harness.addToBattlefield(player1, new Forest()); // keep the pack alive

        Permanent pack = addCreatureReady(player1, new GorillaPack());

        assertThatThrownBy(() -> declareAttackers(
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(pack))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent's Forest does not satisfy the sacrifice condition")
    void opponentForestDoesNotKeepPackAlive() {
        harness.addToBattlefield(player2, new Forest());
        Permanent pack = harness.addToBattlefieldAndReturn(player1, new GorillaPack());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pack);
        harness.assertInGraveyard(player1, "Gorilla Pack");
    }

    @Test
    @DisplayName("Sacrifice trigger reacts when the controller loses their last Forest")
    void sacrificeTriggerReactsWhenLastForestLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent pack = harness.addToBattlefieldAndReturn(player1, new GorillaPack());

        harness.runStateBasedActions();
        assertThat(gd.stack).isEmpty();

        gd.playerBattlefields.get(player1.getId()).remove(forest);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pack);
        harness.assertInGraveyard(player1, "Gorilla Pack");
    }

    @Test
    @DisplayName("Sacrifice trigger sees a Forest subtype removed by a continuous effect")
    void sacrificeTriggerSeesEffectiveForestSubtype() {
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new IllusionaryTerrain());
        terrain.setChosenSubtype(CardSubtype.FOREST);
        terrain.setSecondChosenSubtype(CardSubtype.ISLAND);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent pack = harness.addToBattlefieldAndReturn(player1, new GorillaPack());

        assertThat(gqs.effectiveBasicLandTypes(gd, forest))
                .containsExactly(CardSubtype.ISLAND);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pack);
        harness.assertInGraveyard(player1, "Gorilla Pack");
    }
}
