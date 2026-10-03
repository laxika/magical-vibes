package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchetypeOfCourage.class, YouthfulKnight.class, ZhalfirinKnight.class})
class ArchetypeOfCourageTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures first strike and removes it from opposing creatures")
    void givesFirstStrikeToOwnCreaturesAndRemovesItFromOpponents() {
        Permanent archetype = addCreatureReady(player1, new ArchetypeOfCourage());
        Permanent ownCreature = addCreatureReady(player1, new ZhalfirinKnight());
        Permanent opposingCreature = addCreatureReady(player2, new YouthfulKnight());

        assertThat(gqs.hasKeyword(gd, archetype, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Prevents an opposing creature from gaining first strike")
    void preventsOpposingCreatureFromGainingFirstStrike() {
        addCreatureReady(player1, new ArchetypeOfCourage());
        Permanent opposingKnight = addCreatureReady(player2, new ZhalfirinKnight());
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opposingKnight, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A prevented one-shot first-strike grant does not appear after the restriction leaves")
    void preventedGrantDoesNotAppearAfterRestrictionLeaves() {
        Permanent archetype = addCreatureReady(player1, new ArchetypeOfCourage());
        Permanent opposingKnight = addCreatureReady(player2, new ZhalfirinKnight());
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(archetype);

        assertThat(gqs.hasKeyword(gd, opposingKnight, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opposing Archetypes suppress each other's grants and the surviving grant resumes")
    void opposingArchetypesSuppressGrantsUntilOneLeaves() {
        Permanent firstArchetype = addCreatureReady(player1, new ArchetypeOfCourage());
        Permanent secondArchetype = addCreatureReady(player2, new ArchetypeOfCourage());

        assertThat(gqs.hasKeyword(gd, firstArchetype, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondArchetype, Keyword.FIRST_STRIKE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(firstArchetype);

        assertThat(gqs.hasKeyword(gd, secondArchetype, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Leaving restores printed first strike and ends the grant to your other creatures")
    void leavingRestoresPrintedKeywordAndEndsOwnGrant() {
        Permanent archetype = addCreatureReady(player1, new ArchetypeOfCourage());
        Permanent ownCreature = addCreatureReady(player1, new ZhalfirinKnight());
        Permanent opposingCreature = addCreatureReady(player2, new YouthfulKnight());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FIRST_STRIKE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(archetype);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An existing first-strike grant is suppressed and resumes when the Archetype leaves")
    void existingGrantResumesAfterRestrictionLeaves() {
        Permanent opposingKnight = addCreatureReady(player2, new ZhalfirinKnight());
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opposingKnight, Keyword.FIRST_STRIKE)).isTrue();

        Permanent archetype = addCreatureReady(player1, new ArchetypeOfCourage());
        assertThat(gqs.hasKeyword(gd, opposingKnight, Keyword.FIRST_STRIKE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(archetype);

        assertThat(gqs.hasKeyword(gd, opposingKnight, Keyword.FIRST_STRIKE)).isTrue();
    }
}
