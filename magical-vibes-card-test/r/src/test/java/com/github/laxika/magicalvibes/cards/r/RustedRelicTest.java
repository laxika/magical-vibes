package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.SilverskinArmor;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.TezzeretAgentOfBolas;
import com.github.laxika.magicalvibes.cards.w.WingSplicer;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RustedRelic.class, BottleGnomes.class, GloriousAnthem.class, GrizzlyBears.class,
        LeoninScimitar.class, SilverskinArmor.class, Spellbook.class, WingSplicer.class,
        TezzeretAgentOfBolas.class})
class RustedRelicTest extends BaseCardTest {

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Not a creature with zero other artifacts")
    void notCreatureWithZeroArtifacts() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());

        // Rusted Relic itself is 1 artifact, need 3 total
        assertThat(gqs.isCreature(gd, relic)).isFalse();
    }

    @Test
    @DisplayName("Not a creature with only two total artifacts")
    void notCreatureWithTwoArtifacts() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());

        assertThat(gqs.isCreature(gd, relic)).isFalse();
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Becomes a 5/5 creature with exactly three artifacts")
    void becomesCreatureWithThreeArtifacts() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(5);
    }

    @Test
    @DisplayName("Has Golem subtype with metalcraft active")
    void hasGolemSubtypeWithMetalcraft() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        var bonus = gqs.computeStaticBonus(gd, relic);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.GOLEM);
    }

    @Test
    @DisplayName("Becomes a 5/5 creature with more than three artifacts")
    void becomesCreatureWithFourArtifacts() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(5);
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Stops being a creature when artifact count drops below three")
    void losesCreatureStatusWhenArtifactRemoved() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(5);

        // Remove one artifact — now only 2 total (Rusted Relic + Spellbook)
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Leonin Scimitar"));
        assertThat(gqs.isCreature(gd, relic)).isFalse();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(0);
    }

    // ===== Opponent artifacts don't count =====

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new BottleGnomes());

        assertThat(gqs.isCreature(gd, relic)).isFalse();
    }

    @Test
    @DisplayName("Anthem boosts the animated relic without recursing through metalcraft")
    void anthemBoostsAnimatedRelic() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(6);
    }

    @Test
    @DisplayName("A creature made an artifact by an Equipment counts toward metalcraft")
    void grantedArtifactTypeCountsTowardMetalcraft() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        armor.setAttachedTo(bears.getId());

        // Printed artifacts are only Relic + Armor. Silverskin Armor's layer-4 (CR 613.1d)
        // grant makes the Bears an artifact too, so the true count is 3 and metalcraft is met.

        assertThat(gqs.isArtifact(gd, bears)).isTrue();
        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(5);
    }

    @Test
    @DisplayName("The animated relic is a Golem for another permanent's static grant")
    void animatedRelicIsAGolemForAnotherStaticEffect() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new WingSplicer());

        // Wing Splicer's "Golem creatures you control have flying" is a layer-6 grant whose
        // filter reads the layer-4 subtypes (CR 613.1d/613.1f): the metalcraft animation makes
        // the relic a Golem in layer 4, before the grant's layer.

        assertThat(gqs.hasKeyword(gd, relic, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Without metalcraft the relic is no Golem and gets no grant")
    void unanimatedRelicIsNotAGolem() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new WingSplicer());

        assertThat(gqs.hasKeyword(gd, relic, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An Equipment-granted artifact type turns metalcraft on before the Golem grant")
    void grantedArtifactTypeOrdersAheadOfTheAnimation() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        armor.setAttachedTo(bears.getId());
        harness.addToBattlefield(player1, new WingSplicer());

        // CR 613.8a: the relic's metalcraft animation applies to a different set of objects
        // depending on whether the Armor's layer-4 artifact grant applied first, so it is
        // dependent on the Armor and applies after it — despite the relic's earlier timestamp.

        assertThat(gqs.hasKeyword(gd, relic, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Anthem leaves the relic alone while metalcraft is off")
    void anthemDoesNotBoostRelicWithoutMetalcraft() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.isCreature(gd, relic)).isFalse();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(0);
    }

    @Test
    @DisplayName("Resolving a third Relic animates all three without an additional trigger")
    void thirdRelicAnimatesAllThreeOnResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        assertThat(gqs.isCreature(gd, first)).isFalse();
        assertThat(gqs.isCreature(gd, second)).isFalse();

        harness.castFromHand(player1, new RustedRelic(), "{4}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rusted Relic")).hasSize(3).allSatisfy(relic -> {
            assertThat(gqs.isArtifact(gd, relic)).isTrue();
            assertThat(gqs.isCreature(gd, relic)).isTrue();
            assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(5);
        });
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing the third artifact ends animation before its ability resolves")
    void sacrificeCostImmediatelyEndsMetalcraft() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new BottleGnomes());
        assertThat(gqs.isCreature(gd, relic)).isTrue();

        harness.activateAbility(player1, 2, null, null);

        assertThat(gqs.isCreature(gd, relic)).isFalse();
        assertThat(gqs.isArtifact(gd, relic)).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Rusted Relic");
        harness.assertInGraveyard(player1, "Bottle Gnomes");
    }

    @Test
    @DisplayName("Tezzeret's base 5/5 animation does not add to metalcraft's base 5/5")
    void anotherBasePowerToughnessSetterDoesNotDoubleMetalcraftStats() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent tezzeret = harness.addToBattlefieldAndReturn(player1, new TezzeretAgentOfBolas());
        tezzeret.setCounterCount(CounterType.LOYALTY, 3);
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        harness.activateAbility(player1, 0, 1, null, relic.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(5);
    }
}
