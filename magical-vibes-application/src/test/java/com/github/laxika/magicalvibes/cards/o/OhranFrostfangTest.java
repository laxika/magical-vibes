package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OhranFrostfang.class, GrizzlyBears.class, Forest.class})
class OhranFrostfangTest extends BaseCardTest {

    @Test
    @DisplayName("Gives attacking creatures you control deathtouch")
    void givesAttackingCreaturesDeathtouch() {
        Permanent frostfang = addCreatureReady(player1, new OhranFrostfang());
        Permanent attackingBears = addCreatureReady(player1, new GrizzlyBears());
        frostfang.setAttacking(true);
        attackingBears.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, frostfang, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, attackingBears, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Draws for each creature you control that deals combat damage to a player")
    void drawsForAllyCombatDamage() {
        harness.addToBattlefield(player1, new OhranFrostfang());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }
}
