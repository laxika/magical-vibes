package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MantaRiders;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArchetypeOfImaginationTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures flying and removes it from opposing creatures")
    void givesFlyingToOwnCreaturesAndRemovesItFromOpponents() {
        Permanent archetype = addCreatureReady(player1, new ArchetypeOfImagination());
        Permanent ownCreature = addCreatureReady(player1, new MantaRiders());
        Permanent opposingCreature = addCreatureReady(player2, new AvenWindreader());

        assertThat(gqs.hasKeyword(gd, archetype, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Prevents an opposing creature from gaining flying")
    void preventsOpposingCreatureFromGainingFlying() {
        addCreatureReady(player1, new ArchetypeOfImagination());
        Permanent opposingRiders = addCreatureReady(player2, new MantaRiders());
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opposingRiders, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A prevented one-shot flying grant does not appear after the restriction leaves")
    void preventedGrantDoesNotAppearAfterRestrictionLeaves() {
        Permanent archetype = addCreatureReady(player1, new ArchetypeOfImagination());
        Permanent opposingRiders = addCreatureReady(player2, new MantaRiders());
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(archetype);

        assertThat(gqs.hasKeyword(gd, opposingRiders, Keyword.FLYING)).isFalse();
    }
}
