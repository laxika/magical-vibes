package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CosmicSovereign.class)
class CosmicSovereignTest extends BaseCardTest {

    @Test
    void conjuresACreatureWithTheSourcePowerAndExilesItAtTheNextEndStep() {
        Permanent sovereign = addCreatureReady(player1, new CosmicSovereign());

        resolveBeginningOfCombat(player1);

        List<Permanent> conjured = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(sovereign.getId()))
                .toList();
        assertThat(conjured).hasSize(1);
        Permanent creature = conjured.getFirst();
        assertThat(creature.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(creature.getCard().getManaValue()).isEqualTo(gqs.getEffectivePower(gd, sovereign));
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(creature.getId())
                        && action.kind() == DelayedPermanentActionKind.EXILE_AT_END_STEP);
    }

    @Test
    void redActivationBoostsPowerUntilEndOfTurn() {
        Permanent sovereign = addCreatureReady(player1, new CosmicSovereign());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(3);
    }

    private void resolveBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
