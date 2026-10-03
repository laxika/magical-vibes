package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BantBattlemage;
import com.github.laxika.magicalvibes.cards.y.YavimayaAnts;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchetypeOfAggression.class, BantBattlemage.class, YavimayaAnts.class})
class ArchetypeOfAggressionTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures trample and removes it from opposing creatures")
    void givesTrampleToOwnCreaturesAndRemovesItFromOpponents() {
        Permanent archetype = addCreatureReady(player1, new ArchetypeOfAggression());
        Permanent ownCreature = addCreatureReady(player1, new BantBattlemage());
        Permanent opposingCreature = addCreatureReady(player2, new YavimayaAnts());

        assertThat(gqs.hasKeyword(gd, archetype, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Prevents an opposing creature from gaining trample")
    void preventsOpposingCreatureFromGainingTrample() {
        addCreatureReady(player1, new ArchetypeOfAggression());
        addCreatureReady(player2, new BantBattlemage());
        Permanent opposingCreature = addCreatureReady(player2, new BantBattlemage());
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player2, 0, null, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A prevented one-shot trample grant does not appear after the restriction leaves")
    void preventedGrantDoesNotAppearAfterRestrictionLeaves() {
        Permanent archetype = addCreatureReady(player1, new ArchetypeOfAggression());
        addCreatureReady(player2, new BantBattlemage());
        Permanent opposingCreature = addCreatureReady(player2, new BantBattlemage());
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player2, 0, null, opposingCreature.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(archetype);

        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opposing Archetypes prevent trample regardless of entry order and grants resume when one leaves")
    void opposingArchetypesSuppressEachOtherAndRemainingGrantResumes() {
        Permanent first = addCreatureReady(player1, new ArchetypeOfAggression());
        Permanent second = addCreatureReady(player2, new ArchetypeOfAggression());

        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Opposing Archetypes also suppress trample when the opponent's Archetype enters first")
    void opposingArchetypesSuppressTrampleInReverseEntryOrder() {
        Permanent first = addCreatureReady(player2, new ArchetypeOfAggression());
        Permanent second = addCreatureReady(player1, new ArchetypeOfAggression());

        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Leaving the battlefield removes the grant and restores printed opposing trample")
    void leavingBattlefieldRemovesGrantAndRestoresPrintedTrample() {
        Permanent ownCreature = addCreatureReady(player1, new BantBattlemage());
        Permanent opposingCreature = addCreatureReady(player2, new YavimayaAnts());
        Permanent archetype = addCreatureReady(player1, new ArchetypeOfAggression());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(archetype);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isTrue();
    }
}
