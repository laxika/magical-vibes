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

@CardUsed({SilumgarScavenger.class, GrizzlyBears.class})
class Mh1SilumgarScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit leaves Silumgar Scavenger and the other creature unchanged")
    void decliningExploitDoesNothing() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent scavenger = castScavenger();

        resolveExploitMay(false, fodder);

        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, scavenger, Keyword.HASTE)).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exploiting a creature puts a counter on Silumgar Scavenger and gives it haste")
    void exploitingCreaturePutsCounterAndGivesHaste() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent scavenger = castScavenger();

        resolveExploitMay(true, fodder);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, scavenger, Keyword.HASTE)).isTrue();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Another creature's death puts a counter on Silumgar Scavenger without granting haste")
    void anotherCreatureDeathOnlyPutsCounter() {
        Permanent scavenger = addReadyScavenger();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, creature));
        harness.passBothPriorities();

        assertThat(scavenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, scavenger, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste from exploit wears off at end of turn")
    void exploitHasteWearsOffAtEndOfTurn() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent scavenger = castScavenger();

        resolveExploitMay(true, fodder);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, scavenger, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scavenger, Keyword.HASTE)).isFalse();
    }

    private Permanent castScavenger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SilumgarScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Silumgar Scavenger");
    }

    private Permanent addReadyScavenger() {
        Permanent scavenger = harness.addToBattlefieldAndReturn(player1, new SilumgarScavenger());
        scavenger.setSummoningSick(false);
        return scavenger;
    }

    private void resolveExploitMay(boolean accept, Permanent fodder) {
        harness.handleMayAbilityChosen(player1, accept);
        if (accept) {
            harness.handlePermanentChosen(player1, fodder.getId());
        }
    }
}
