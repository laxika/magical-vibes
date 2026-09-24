package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CloudfinRaptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MutablePupa.class, CloudfinRaptor.class, GrizzlyBears.class, Shock.class})
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
        harness.castInstant(player1, 0, raptor.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pupa, Keyword.FLYING)).isTrue();
    }
}
