package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CloudfinRaptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.j.Jump;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MutablePupa.class, CloudfinRaptor.class, GrizzlyBears.class, Shock.class, Jump.class, Unsummon.class})
class MutablePupaTest extends BaseCardTest {

    @Test
    void perpetuallyGainsFlyingFromAnotherFlyingCreature() {
        Permanent pupa = harness.addToBattlefieldAndReturn(player1, new MutablePupa());

        harness.setHand(player1, List.of(new CloudfinRaptor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pupa, Keyword.FLYING)).isTrue();
    }

    @Test
    void doesNotGainKeywordsFromACreatureWithoutSupportedKeywords() {
        Permanent pupa = harness.addToBattlefieldAndReturn(player1, new MutablePupa());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pupa, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, pupa, Keyword.REACH)).isFalse();
    }

    @Test
    void usesLastKnownKeywordsIfTheEnteringCreatureLeavesBeforeResolution() {
        Permanent pupa = harness.addToBattlefieldAndReturn(player1, new MutablePupa());

        harness.setHand(player1, List.of(new CloudfinRaptor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent raptor = findPermanent(player1, "Cloudfin Raptor");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, raptor.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pupa, Keyword.FLYING)).isTrue();
    }

    @Test
    void evolvesWhenAnotherCreatureHasGreaterPowerOrToughness() {
        Permanent pupa = harness.addToBattlefieldAndReturn(player1, new MutablePupa());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pupa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForItsOwnEntry() {
        Permanent pupa = harness.enterBattlefieldAndReturn(player1, new MutablePupa());

        assertThat(gd.stack).isEmpty();
        assertThat(pupa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotGainKeywordsFromAnOpponentsCreature() {
        Permanent pupa = harness.addToBattlefieldAndReturn(player1, new MutablePupa());

        harness.enterBattlefieldAndReturn(player2, new CloudfinRaptor());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, pupa, Keyword.FLYING)).isFalse();
    }

    @Test
    void usesKeywordsGainedAfterEntryWhenTheCreatureIsStillPresent() {
        Permanent pupa = harness.addToBattlefieldAndReturn(player1, new MutablePupa());
        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Jump()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pupa, Keyword.FLYING)).isTrue();
    }

    @Test
    void usesKeywordsGainedAfterEntryWhenTheCreatureDiesBeforeResolution() {
        Permanent pupa = harness.addToBattlefieldAndReturn(player1, new MutablePupa());
        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Jump(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pupa, Keyword.FLYING)).isTrue();
    }

    @Test
    void keepsPerpetuallyGainedFlyingAfterReturningToHandAndBeingRecast() {
        Permanent pupa = harness.addToBattlefieldAndReturn(player1, new MutablePupa());
        harness.enterBattlefieldAndReturn(player1, new CloudfinRaptor());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, pupa, Keyword.FLYING)).isTrue();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, pupa.getId());
        harness.assertInHand(player1, "Mutable Pupa");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent recastPupa = findPermanent(player1, "Mutable Pupa");
        assertThat(gqs.hasKeyword(gd, recastPupa, Keyword.FLYING)).isTrue();
    }
}
