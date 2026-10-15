package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.Expel;
import com.github.laxika.magicalvibes.cards.j.JourneyToNowhere;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StonebindersFamiliar.class, GrizzlyBears.class, JourneyToNowhere.class, Expel.class})
class StonebindersFamiliarTest extends BaseCardTest {

    @Test
    void getsACounterWhenACardIsExiledDuringYourTurn() {
        harness.addToBattlefield(player1, new StonebindersFamiliar());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent familiar = findPermanent(player1, "Stonebinder's Familiar");

        exileWithJourney(harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(familiar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggersOnlyOncePerTurn() {
        harness.addToBattlefield(player1, new StonebindersFamiliar());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent familiar = findPermanent(player1, "Stonebinder's Familiar");

        exileWithJourney(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.addToBattlefield(player2, new GrizzlyBears());
        exileWithJourney(harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(familiar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringAnOpponentsTurn() {
        harness.addToBattlefield(player1, new StonebindersFamiliar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent familiar = findPermanent(player1, "Stonebinder's Familiar");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new JourneyToNowhere()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castEnchantment(player2, 0,
                harness.getPermanentId(player1, "Grizzly Bears"));
        resolveAllTriggers();

        assertThat(familiar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentExilingYourCardDuringYourTurnTriggersEachRemainingFamiliar() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new StonebindersFamiliar());
        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addToBattlefield(player1, new StonebindersFamiliar());
        Permanent second = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.addToBattlefield(player1, new StonebindersFamiliar());
        Permanent target = gd.playerBattlefields.get(player1.getId()).get(2);
        target.tap();
        harness.setHand(player2, List.of(new Expel()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void secondExileDoesNotTriggerWhileFirstTriggerIsStillPending() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new StonebindersFamiliar());
        Permanent familiar = findPermanent(player1, "Stonebinder's Familiar");
        harness.addToBattlefield(player2, new StonebindersFamiliar());
        Permanent firstTarget = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.addToBattlefield(player2, new StonebindersFamiliar());
        Permanent secondTarget = gd.playerBattlefields.get(player2.getId()).get(1);
        firstTarget.tap();
        secondTarget.tap();
        harness.setHand(player1, List.of(new Expel(), new Expel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, firstTarget.getId());
        assertThat(familiar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.castAndResolveInstant(player1, 0, secondTarget.getId());
        resolveAllTriggers();

        assertThat(familiar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(firstTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(secondTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void exileWithJourney(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new JourneyToNowhere()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, targetId);
        resolveAllTriggers();
    }
}
