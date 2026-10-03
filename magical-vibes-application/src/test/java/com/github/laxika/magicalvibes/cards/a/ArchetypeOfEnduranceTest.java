package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HorrorOfTheDim;
import com.github.laxika.magicalvibes.cards.x.XathridSlyblade;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchetypeOfEndurance.class, HorrorOfTheDim.class, XathridSlyblade.class})
class ArchetypeOfEnduranceTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures hexproof and removes it from opposing creatures")
    void givesHexproofToOwnCreaturesAndRemovesItFromOpponents() {
        Permanent archetype = addCreatureReady(player1, new ArchetypeOfEndurance());
        Permanent ownCreature = addCreatureReady(player1, new HorrorOfTheDim());
        Permanent opposingCreature = addCreatureReady(player2, new XathridSlyblade());

        assertThat(gqs.hasKeyword(gd, archetype, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Prevents an opposing creature from gaining hexproof")
    void preventsOpposingCreatureFromGainingHexproof() {
        addCreatureReady(player1, new ArchetypeOfEndurance());
        Permanent opposingCreature = addCreatureReady(player2, new HorrorOfTheDim());
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("A prevented one-shot hexproof grant does not appear after the restriction leaves")
    void preventedGrantDoesNotAppearAfterRestrictionLeaves() {
        Permanent archetype = addCreatureReady(player1, new ArchetypeOfEndurance());
        Permanent opposingCreature = addCreatureReady(player2, new HorrorOfTheDim());
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(archetype);

        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Opposing Archetypes prevent both players' creatures from having hexproof")
    void opposingArchetypesPreventHexproofRegardlessOfEntryOrder() {
        Permanent firstArchetype = addCreatureReady(player1, new ArchetypeOfEndurance());
        assertThat(gqs.hasKeyword(gd, firstArchetype, Keyword.HEXPROOF)).isTrue();

        Permanent secondArchetype = addCreatureReady(player2, new ArchetypeOfEndurance());

        assertThat(gqs.hasKeyword(gd, firstArchetype, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondArchetype, Keyword.HEXPROOF)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(firstArchetype);

        assertThat(gqs.hasKeyword(gd, secondArchetype, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Printed hexproof returns and your granted hexproof ends when Archetype leaves")
    void printedHexproofReturnsWhenArchetypeLeaves() {
        Permanent archetype = addCreatureReady(player1, new ArchetypeOfEndurance());
        Permanent ownCreature = addCreatureReady(player1, new HorrorOfTheDim());
        Permanent opposingCreature = addCreatureReady(player2, new XathridSlyblade());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HEXPROOF)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(archetype);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Hexproof gained before Archetype enters resumes when Archetype leaves")
    void previouslyGrantedHexproofReturnsWhenArchetypeLeaves() {
        Permanent opposingCreature = addCreatureReady(player2, new HorrorOfTheDim());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HEXPROOF)).isTrue();

        Permanent archetype = addCreatureReady(player1, new ArchetypeOfEndurance());
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HEXPROOF)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(archetype);

        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HEXPROOF)).isTrue();
    }
}
