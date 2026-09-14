package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FatalPush;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilumgarScavenger.class, GrizzlyBears.class, FatalPush.class})
class SilumgarScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control dying puts a +1/+1 counter on Silumgar Scavenger")
    void allyCreatureDeathPutsCounterOnScavenger() {
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new SilumgarScavenger());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FatalPush()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, fodder.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, scavenger, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Exploiting a creature puts a counter on Silumgar Scavenger and grants haste")
    void exploitingCreaturePutsCounterAndGrantsHaste() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castScavenger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent scavenger = findPermanent(player1, "Silumgar Scavenger");
        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, scavenger, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scavenger, Keyword.HASTE)).isFalse();
    }

    private void castScavenger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SilumgarScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
