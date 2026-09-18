package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BattleScreech;
import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
import com.github.laxika.magicalvibes.cards.t.ToxicStench;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulcatchersAerie.class, SuntailHawk.class, BenevolentBodyguard.class,
        ToxicStench.class, BattleScreech.class})
class SoulcatchersAerieTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a feather counter on itself when a Bird is put into its controller's graveyard")
    void putsFeatherCounterWhenBirdDies() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());

        killWithToxicStench(player1, bird);

        assertThat(aerie.getCounterCount(CounterType.FEATHER)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a non-Bird creature")
    void doesNotTriggerForNonBird() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        Permanent nonBird = harness.addToBattlefieldAndReturn(player1, new BenevolentBodyguard());

        killWithToxicStench(player1, nonBird);

        assertThat(aerie.getCounterCount(CounterType.FEATHER)).isZero();
    }

    @Test
    @DisplayName("Does not trigger for a Bird put into an opponent's graveyard")
    void doesNotTriggerForOpponentsBird() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        Permanent bird = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        killWithToxicStench(player1, bird);

        assertThat(aerie.getCounterCount(CounterType.FEATHER)).isZero();
    }

    @Test
    @DisplayName("Bird creatures get +1/+1 for each feather counter")
    void boostsBirdsByFeatherCounterCount() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        aerie.setCounterCount(CounterType.FEATHER, 2);

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(3);
    }

    @Test
    @DisplayName("The bonus applies to Birds controlled by either player, but not other creatures")
    void boostsBirdsOnBothBattlefieldsButNotNonBirds() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());
        aerie.setCounterCount(CounterType.FEATHER, 2);
        Permanent opponentBird = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent nonBird = harness.addToBattlefieldAndReturn(player1, new BenevolentBodyguard());

        assertThat(gqs.getEffectivePower(gd, opponentBird)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentBird)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonBird)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonBird)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a feather counter on itself when a Bird token is put into its controller's graveyard")
    void putsFeatherCounterWhenBirdTokenDies() {
        Permanent aerie = harness.addToBattlefieldAndReturn(player1, new SoulcatchersAerie());

        harness.castFromHand(player1, new BattleScreech(), "{2}{W}{W}");
        harness.passBothPriorities();

        List<Permanent> birds = findPermanents(player1, "Bird");
        assertThat(birds).hasSize(2);

        killWithToxicStench(player1, birds.get(0));

        assertThat(aerie.getCounterCount(CounterType.FEATHER)).isEqualTo(1);
    }

    private void killWithToxicStench(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new ToxicStench()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
