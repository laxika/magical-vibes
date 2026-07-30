package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.SilverskinArmor;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.w.WingSplicer;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RustedRelicTest extends BaseCardTest {

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Not a creature with zero other artifacts")
    void notCreatureWithZeroArtifacts() {
        harness.addToBattlefield(player1, new RustedRelic());

        Permanent relic = gd.playerBattlefields.get(player1.getId()).getFirst();
        // Rusted Relic itself is 1 artifact, need 3 total
        assertThat(gqs.isCreature(gd, relic)).isFalse();
    }

    @Test
    @DisplayName("Not a creature with only two total artifacts")
    void notCreatureWithTwoArtifacts() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent relic = findPermanent(player1, "Rusted Relic");
        assertThat(gqs.isCreature(gd, relic)).isFalse();
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Becomes a 5/5 creature with exactly three artifacts")
    void becomesCreatureWithThreeArtifacts() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent relic = findPermanent(player1, "Rusted Relic");
        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(5);
    }

    @Test
    @DisplayName("Has Golem subtype with metalcraft active")
    void hasGolemSubtypeWithMetalcraft() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent relic = findPermanent(player1, "Rusted Relic");
        var bonus = gqs.computeStaticBonus(gd, relic);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.GOLEM);
    }

    @Test
    @DisplayName("Becomes a 5/5 creature with more than three artifacts")
    void becomesCreatureWithFourArtifacts() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent relic = findPermanent(player1, "Rusted Relic");
        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(5);
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Stops being a creature when artifact count drops below three")
    void losesCreatureStatusWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent relic = findPermanent(player1, "Rusted Relic");
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
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new BottleGnomes());

        Permanent relic = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, relic)).isFalse();
    }

    @Test
    @DisplayName("Anthem boosts the animated relic without recursing through metalcraft")
    void anthemBoostsAnimatedRelic() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new GloriousAnthem());

        Permanent relic = findPermanent(player1, "Rusted Relic");
        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(6);
    }

    @Test
    @DisplayName("A creature made an artifact by an Equipment counts toward metalcraft")
    void grantedArtifactTypeCountsTowardMetalcraft() {
        harness.addToBattlefield(player1, new RustedRelic());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        armor.setAttachedTo(bears.getId());

        // Printed artifacts are only Relic + Armor. Silverskin Armor's layer-4 (CR 613.1d)
        // grant makes the Bears an artifact too, so the true count is 3 and metalcraft is met.
        Permanent relic = findPermanent(player1, "Rusted Relic");
        assertThat(gqs.isArtifact(gd, bears)).isTrue();
        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(5);
    }

    @Test
    @DisplayName("The animated relic is a Golem for another permanent's static grant")
    void animatedRelicIsAGolemForAnotherStaticEffect() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new WingSplicer());

        // Wing Splicer's "Golem creatures you control have flying" is a layer-6 grant whose
        // filter reads the layer-4 subtypes (CR 613.1d/613.1f): the metalcraft animation makes
        // the relic a Golem in layer 4, before the grant's layer.
        Permanent relic = findPermanent(player1, "Rusted Relic");
        assertThat(gqs.hasKeyword(gd, relic, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Without metalcraft the relic is no Golem and gets no grant")
    void unanimatedRelicIsNotAGolem() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new WingSplicer());

        Permanent relic = findPermanent(player1, "Rusted Relic");
        assertThat(gqs.hasKeyword(gd, relic, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An Equipment-granted artifact type turns metalcraft on before the Golem grant")
    void grantedArtifactTypeOrdersAheadOfTheAnimation() {
        harness.addToBattlefield(player1, new RustedRelic());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        armor.setAttachedTo(bears.getId());
        harness.addToBattlefield(player1, new WingSplicer());

        // CR 613.8a: the relic's metalcraft animation applies to a different set of objects
        // depending on whether the Armor's layer-4 artifact grant applied first, so it is
        // dependent on the Armor and applies after it — despite the relic's earlier timestamp.
        Permanent relic = findPermanent(player1, "Rusted Relic");
        assertThat(gqs.hasKeyword(gd, relic, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Anthem leaves the relic alone while metalcraft is off")
    void anthemDoesNotBoostRelicWithoutMetalcraft() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new GloriousAnthem());

        Permanent relic = findPermanent(player1, "Rusted Relic");
        assertThat(gqs.isCreature(gd, relic)).isFalse();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(0);
    }
}
