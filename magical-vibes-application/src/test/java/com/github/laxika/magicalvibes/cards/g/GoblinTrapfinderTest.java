package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinTrapfinder.class, GrizzlyBears.class, Murder.class})
class GoblinTrapfinderTest extends BaseCardTest {

    @Test
    void deathSeeksCreatureWithPerpetualHasteCostReductionAndSacrificeTrigger() {
        Permanent trapfinder = harness.addToBattlefieldAndReturn(player1, new GoblinTrapfinder());
        Card sought = new GrizzlyBears();
        harness.setLibrary(player1, List.of(sought));
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, trapfinder.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(sought.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sought);
    }
}
