package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KarametrasBlessing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroOfThePride.class, GiantGrowth.class, GrizzlyBears.class, KarametrasBlessing.class})
class HeroOfThePrideTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures when you cast a spell targeting Hero of the Pride")
    void boostsYourCreaturesWhenTargetedByOwnSpell() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfThePride());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGiantGrowth(player1, hero);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when your spell targets another creature")
    void doesNotTriggerWhenAnotherCreatureIsTargeted() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfThePride());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGiantGrowth(player1, bears);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a spell targeting it")
    void doesNotTriggerOnOpponentsSpell() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfThePride());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGiantGrowth(player2, hero);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger resolves before the spell and boosts only creatures present at resolution")
    void boostsCreaturesPresentAtTriggerResolution() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfThePride());
        Permanent opponentHero = harness.addToBattlefieldAndReturn(player2, new HeroOfThePride());
        harness.setHand(player1, List.of(new KarametrasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, hero.getId());

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        Permanent earlyArrival = harness.addToBattlefieldAndReturn(player1, new HeroOfThePride());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, earlyArrival)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentHero)).isEqualTo(2);

        Permanent lateArrival = harness.addToBattlefieldAndReturn(player1, new HeroOfThePride());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, earlyArrival)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, earlyArrival)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, lateArrival)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each spell targeting the Hero adds another team boost")
    void repeatedTargetingSpellsStackBoosts() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfThePride());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new HeroOfThePride());
        harness.setHand(player1, List.of(new KarametrasBlessing(), new KarametrasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, hero.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, hero.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);
    }

    @Test
    @DisplayName("The team boost wears off at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfThePride());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new HeroOfThePride());
        harness.setHand(player1, List.of(new KarametrasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, hero.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);
    }

    private void castGiantGrowth(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        harness.setHand(player, List.of(new GiantGrowth()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player);
        harness.castInstant(player, 0, target.getId());
        resolveAllTriggers();
    }
}
