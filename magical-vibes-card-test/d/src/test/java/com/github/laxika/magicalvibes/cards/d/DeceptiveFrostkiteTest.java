package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeceptiveFrostkite.class, ColossalDreadmaw.class, GrizzlyBears.class})
class DeceptiveFrostkiteTest extends BaseCardTest {

    @Test
    void copiesOnlyAControlledCreatureWithPowerFourOrGreaterAndAddsExceptions() {
        Permanent eligible = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Permanent tooSmall = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentEligible = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        DeceptiveFrostkite frostkite = new DeceptiveFrostkite();

        harness.castFromHand(player1, frostkite, "{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(eligible.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(tooSmall.getId(), opponentEligible.getId());

        harness.handlePermanentChosen(player1, eligible.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(frostkite.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.effectiveCreatureSubtypes(gd, copy)).contains(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.FLYING)).isTrue();
    }

    @Test
    void mayDeclineToCopyAnEligibleCreature() {
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        harness.castFromHand(player1, new DeceptiveFrostkite(), "{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Deceptive Frostkite");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void entersWithoutCopyingWhenOnlySmallOrOpposingCreaturesExist() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.castFromHand(player1, new DeceptiveFrostkite(), "{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Deceptive Frostkite");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void countersCountForEligibilityButAreNotCopied() {
        Permanent eligible = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        eligible.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent tooSmall = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tooSmall.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        DeceptiveFrostkite frostkite = new DeceptiveFrostkite();
        harness.castFromHand(player1, frostkite, "{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(eligible.getId());
        harness.handlePermanentChosen(player1, eligible.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(frostkite.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, copy)).contains(CardSubtype.BEAR, CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.FLYING)).isTrue();
    }

    @Test
    void copyingAnotherFrostkitePreservesItsCopyExceptions() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        DeceptiveFrostkite first = new DeceptiveFrostkite();
        harness.castFromHand(player1, first, "{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());
        Permanent firstCopy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(first.getId()))
                .findFirst().orElseThrow();

        DeceptiveFrostkite second = new DeceptiveFrostkite();
        harness.castFromHand(player1, second, "{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstCopy.getId());

        Permanent secondCopy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(second.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.effectiveCreatureSubtypes(gd, secondCopy)).contains(CardSubtype.DINOSAUR, CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, secondCopy, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCopy, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, secondCopy)).isEqualTo(gqs.getEffectivePower(gd, original));
    }
}
