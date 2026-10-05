package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.FloodedStrand;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PreponderantPearl.class, CoralMerfolk.class, FloodedStrand.class,
        GrizzlyBears.class, Ponder.class})
class PreponderantPearlTest extends BaseCardTest {

    @Test
    void entersAndConjuresPonderIntoHand() {
        harness.enterBattlefieldAndReturn(player1, new PreponderantPearl());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anySatisfy(card -> {
                    assertThat(card.getName()).isEqualTo("Ponder");
                    assertThat(card.isToken()).isFalse();
                });
    }

    @Test
    void merfolkCombatDamageSacrificesPearlAndConjuresFloodedStrandOnce() {
        Permanent pearl = addCreatureReady(player1, new PreponderantPearl());
        addCreatureReady(player1, new CoralMerfolk());
        addCreatureReady(player1, new CoralMerfolk());

        declareAttackers(List.of(1, 2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pearl);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pearl.getCard());
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Flooded Strand"))
                .hasSize(1)
                .allSatisfy(card -> assertThat(card.isToken()).isFalse());
    }

    @Test
    void nonMerfolkCombatDamageDoesNotTriggerPearl() {
        Permanent pearl = addCreatureReady(player1, new PreponderantPearl());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pearl);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Flooded Strand"));
    }

    @Test
    void opposingMerfolkCombatDamageDoesNotTriggerPearl() {
        Permanent pearl = harness.addToBattlefieldAndReturn(player1, new PreponderantPearl());
        addCreatureReady(player2, new CoralMerfolk());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pearl);
        harness.assertNotInHand(player1, "Flooded Strand");
        harness.assertNotInHand(player2, "Flooded Strand");
    }

    @Test
    void blockedMerfolkDealingDamageToCreatureDoesNotTriggerPearl() {
        Permanent pearl = harness.addToBattlefieldAndReturn(player1, new PreponderantPearl());
        addCreatureReady(player1, new CoralMerfolk());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pearl);
        harness.assertNotInHand(player1, "Flooded Strand");
    }

    @Test
    void eachPearlSacrificesItselfAndConjuresItsOwnFloodedStrand() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PreponderantPearl());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PreponderantPearl());
        addCreatureReady(player1, new CoralMerfolk());

        declareAttackers(List.of(2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first.getCard(), second.getCard());
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Flooded Strand"))
                .hasSize(2)
                .allSatisfy(card -> assertThat(card.getOwnerId()).isEqualTo(player1.getId()));
    }

    @Test
    void simultaneousMerfolkCombatDamageCreatesOnlyOneTrigger() {
        harness.addToBattlefield(player1, new PreponderantPearl());
        addCreatureReady(player1, new CoralMerfolk());
        addCreatureReady(player1, new CoralMerfolk());

        declareAttackersAndPrepareBlockers(List.of(1, 2));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 16);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Flooded Strand"))
                .hasSize(1);
    }

    @Test
    void pearlRemovedBeforeTriggerResolvesDoesNotConjureFloodedStrand() {
        Permanent pearl = harness.addToBattlefieldAndReturn(player1, new PreponderantPearl());
        addCreatureReady(player1, new CoralMerfolk());

        declareAttackersAndPrepareBlockers(List.of(1));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, pearl));
        resolveAllTriggers();

        harness.assertInHand(player1, "Preponderant Pearl");
        harness.assertNotInHand(player1, "Flooded Strand");
        harness.assertNotInGraveyard(player1, "Preponderant Pearl");
    }
}
