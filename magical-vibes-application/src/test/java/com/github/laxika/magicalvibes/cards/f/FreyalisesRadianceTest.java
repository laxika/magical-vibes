package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FreyalisesRadianceTest extends BaseCardTest {

    @Test
    @DisplayName("Snow permanents do not untap during their controllers' untap steps")
    void snowPermanentsDoNotUntap() {
        addReady(player1, new FreyalisesRadiance());
        Permanent snowLand = addReady(player2, new SnowCoveredForest());
        snowLand.tap();

        advanceToNextTurn(player1);

        assertThat(snowLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-snow permanents untap normally")
    void nonSnowPermanentsUntap() {
        addReady(player1, new FreyalisesRadiance());
        Permanent creature = addReady(player2, new GrizzlyBears());
        creature.tap();

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Freyalise's Radiance on the battlefield")
    void payingCumulativeUpkeepKeepsRadiance() {
        Permanent radiance = addReady(player1, new FreyalisesRadiance());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(radiance.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(radiance);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Freyalise's Radiance")
    void decliningCumulativeUpkeepSacrificesRadiance() {
        Permanent radiance = addReady(player1, new FreyalisesRadiance());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(radiance);
        harness.assertInGraveyard(player1, "Freyalise's Radiance");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
