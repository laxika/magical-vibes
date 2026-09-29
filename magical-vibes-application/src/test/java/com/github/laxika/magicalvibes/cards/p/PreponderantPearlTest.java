package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.FloodedStrand;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
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
}
