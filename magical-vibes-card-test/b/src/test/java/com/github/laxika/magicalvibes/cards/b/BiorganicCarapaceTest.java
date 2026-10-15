package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RoboticsMastery;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BiorganicCarapace.class, GrizzlyBears.class, Island.class, RoboticsMastery.class})
class BiorganicCarapaceTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Biorganic Carapace attaches it and gives the creature +2/+2")
    void enteringAttachesAndBoostsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castCarapace(creature);

        Permanent carapace = findPermanent(player1, "Biorganic Carapace");
        assertThat(carapace.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Combat damage draws for each modified creature controlled")
    void combatDamageDrawsForEachModifiedCreature() {
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent counteredCreature = addCreatureReady(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new GrizzlyBears());
        castCarapace(equippedCreature);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(equippedCreature)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Equip moves the boost and granted ability to the new creature")
    void equipMovesBoostAndDrawAbility() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        castCarapace(first);
        Permanent carapace = findPermanent(player1, "Biorganic Carapace");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(carapace),
                null, second.getId());
        resolveAllTriggers();

        assertThat(carapace.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Counters on noncreatures do not increase the draw count")
    void counteredNoncreaturesAreNotCounted() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castCarapace(creature);
        findPermanent(player1, "Biorganic Carapace").setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Multiple modifications count a creature once and opposing creatures are excluded")
    void countsEachControlledModifiedCreatureOnce() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castCarapace(creature);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Only Auras controlled by the creature's controller modify it")
    void auraControllerDeterminesModification() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownEnchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingEnchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new RoboticsMastery());
        ownAura.setAttachedTo(ownEnchanted.getId());
        Permanent opposingAura = harness.addToBattlefieldAndReturn(player2, new RoboticsMastery());
        opposingAura.setAttachedTo(opposingEnchanted.getId());
        castCarapace(attacker);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    private void castCarapace(Permanent creature) {
        harness.setHand(player1, List.of(new BiorganicCarapace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();
    }
}
