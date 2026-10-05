package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmniChangeling.class, GoblinKing.class, GrizzlyBears.class, AngelOfMercy.class})
class OmniChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Omni-Changeling keeps changeling when it copies a creature")
    void keepsChangelingWhenCopyingCreature() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new OmniChangeling(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent copied = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, copied, Keyword.CHANGELING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, copied)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copied)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining to copy leaves a 0/0 that dies")
    void decliningToCopyDies() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new OmniChangeling(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Omni-Changeling");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining to copy can survive thanks to changeling and a Goblin lord")
    void decliningToCopySurvivesWithTribalBonus() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.castFromHand(player1, new OmniChangeling(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent changeling = findPermanent(player1, "Omni-Changeling");
        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.MOUNTAINWALK)).isTrue();
        harness.assertNotInGraveyard(player1, "Omni-Changeling");
    }

    @Test
    @DisplayName("Without any creatures to copy Omni-Changeling dies")
    void noCreaturesToCopyDies() {
        harness.castFromHand(player1, new OmniChangeling(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Omni-Changeling");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Copying your own creature does not copy counters or tapped state")
    void copiesOwnCreatureWithoutCountersOrTappedState() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        bears.tap();
        harness.castFromHand(player1, new OmniChangeling(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent copied = findPermanents(player1, "Grizzly Bears").getLast();
        assertThat(gqs.getEffectivePower(gd, copied)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copied)).isEqualTo(2);
        assertThat(copied.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(copied.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, copied, Keyword.CHANGELING)).isTrue();
    }

    @Test
    @DisplayName("Copied flying and enter abilities work alongside changeling")
    void copiesKeywordsAndEnterAbility() {
        harness.addToBattlefield(player2, new AngelOfMercy());
        harness.castFromHand(player1, new OmniChangeling(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Angel of Mercy"));
        resolveAllTriggers();

        Permanent copied = findPermanent(player1, "Angel of Mercy");
        assertThat(gqs.hasKeyword(gd, copied, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, copied, Keyword.CHANGELING)).isTrue();
        assertThat(gqs.hasKeyword(gd, copied, Keyword.CONVOKE)).isFalse();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The changeling copy exception is itself copied by another Omni-Changeling")
    void copyingAnotherCopyKeepsChangeling() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new OmniChangeling(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        Permanent firstCopy = findPermanent(player1, "Grizzly Bears");

        harness.castFromHand(player1, new OmniChangeling(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstCopy.getId());

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        Permanent secondCopy = findPermanents(player1, "Grizzly Bears").getLast();
        assertThat(gqs.hasKeyword(gd, secondCopy, Keyword.CHANGELING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, secondCopy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondCopy)).isEqualTo(2);
    }

    @Test
    @DisplayName("Convoke can pay the generic cost with summoning-sick creatures")
    void convokesUsingSummoningSickCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OmniChangeling()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(first.getId(), second.getId(), third.getId()));
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, first.getId());

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(4);
        Permanent copied = findPermanents(player1, "Grizzly Bears").getLast();
        assertThat(copied.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, copied, Keyword.CHANGELING)).isTrue();
    }
}
