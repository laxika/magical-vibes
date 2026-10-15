package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BolaSlinger;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.SunderTheGateway;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({PhyrexianAwakening.class, PhyrexianCensor.class, BolaSlinger.class})
class PhyrexianAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with an Incubator token with four +1/+1 counters")
    void entersWithIncubatorToken() {
        castAwakening();
        resolveAwakening();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gives vigilance to Phyrexian creatures you control only")
    void givesVigilanceToOwnPhyrexians() {
        Permanent phyrexian = harness.addToBattlefieldAndReturn(player1, new PhyrexianCensor());
        Permanent nonPhyrexian = harness.addToBattlefieldAndReturn(player1, new BolaSlinger());
        harness.addToBattlefield(player1, new PhyrexianAwakening());

        assertThat(gqs.hasKeyword(gd, phyrexian, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonPhyrexian, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The Incubator transforms into a vigilant Phyrexian")
    void incubatorTransformsIntoVigilantPhyrexian() {
        castAwakening();
        resolveAwakening();

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.hasKeyword(gd, incubator, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void incubationWaitsForEnterTriggerToResolve() {
        castAwakening();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phyrexian Awakening");
        harness.assertNotOnBattlefield(player1, "Incubator");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(gqs.isCreature(gd, incubator)).isFalse();
        assertThat(gqs.hasKeyword(gd, incubator, Keyword.VIGILANCE)).isFalse();
        assertThat(incubator.getCard().getSubtypes()).containsExactly(CardSubtype.INCUBATOR);
        assertThat(incubator.getCard().getColors()).isEmpty();
    }

    @Test
    void doesNotGiveVigilanceToOpposingPhyrexians() {
        Permanent opponentPhyrexian = harness.addToBattlefieldAndReturn(player2, new PhyrexianCensor());
        harness.addToBattlefield(player1, new PhyrexianAwakening());

        assertThat(gqs.hasKeyword(gd, opponentPhyrexian, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void transformedTokenRetainsCountersAndBecomesColorlessArtifactCreature() {
        castAwakening();
        resolveAwakening();
        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(incubator);
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(gqs.isArtifact(gd, incubator)).isTrue();
        assertThat(incubator.getCard().getSubtypes()).containsExactly(CardSubtype.PHYREXIAN);
        assertThat(incubator.getCard().getColors()).isEmpty();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(4);
    }

    @Test
    void twoPendingTransformActivationsDoNotTransformTokenBack() {
        castAwakening();
        resolveAwakening();
        Permanent incubator = findPermanent(player1, "Incubator");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(incubator);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, index, null, null);
        harness.activateAbility(player1, index, null, null);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, incubator, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @CardUsed({SunderTheGateway.class})
    void tokenSurvivesButLosesVigilanceWhenAwakeningIsDestroyed() {
        castAwakening();
        resolveAwakening();
        Permanent awakening = findPermanent(player1, "Phyrexian Awakening");
        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, incubator, Keyword.VIGILANCE)).isTrue();

        harness.setHand(player2, List.of(new SunderTheGateway()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player2);
        harness.castSorcery(player2, 0, 0, awakening.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Awakening");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(incubator);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, incubator, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @CardUsed({Opalescence.class, MaskwoodNexus.class})
    void animatedPhyrexianAwakeningGivesItselfVigilance() {
        Permanent awakening = harness.addToBattlefieldAndReturn(player1, new PhyrexianAwakening());
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new MaskwoodNexus());

        assertThat(gqs.isCreature(gd, awakening)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, awakening).grantedSubtypes()).contains(CardSubtype.PHYREXIAN);
        assertThat(gqs.hasKeyword(gd, awakening, Keyword.VIGILANCE)).isTrue();
    }

    private void castAwakening() {
        harness.castFromHand(player1, new PhyrexianAwakening(), "{2}{W}");
    }

    private void resolveAwakening() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
