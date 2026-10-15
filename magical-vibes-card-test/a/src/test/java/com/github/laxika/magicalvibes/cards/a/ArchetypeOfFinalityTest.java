package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BalefulEidolon;
import com.github.laxika.magicalvibes.cards.f.FetidImp;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchetypeOfFinality.class, FetidImp.class, BalefulEidolon.class})
class ArchetypeOfFinalityTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures deathtouch and removes it from opposing creatures")
    void givesDeathtouchToOwnCreaturesAndRemovesItFromOpponents() {
        Permanent archetype = addCreatureReady(player1, new ArchetypeOfFinality());
        Permanent ownCreature = addCreatureReady(player1, new FetidImp());
        Permanent opposingCreature = addCreatureReady(player2, new BalefulEidolon());

        assertThat(gqs.hasKeyword(gd, archetype, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Prevents an opposing creature from gaining deathtouch")
    void preventsOpposingCreatureFromGainingDeathtouch() {
        addCreatureReady(player1, new ArchetypeOfFinality());
        Permanent opposingImp = addCreatureReady(player2, new FetidImp());
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opposingImp, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A prevented one-shot deathtouch grant does not appear after the restriction leaves")
    void preventedGrantDoesNotAppearAfterRestrictionLeaves() {
        Permanent archetype = addCreatureReady(player1, new ArchetypeOfFinality());
        Permanent opposingImp = addCreatureReady(player2, new FetidImp());
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(archetype);

        assertThat(gqs.hasKeyword(gd, opposingImp, Keyword.DEATHTOUCH)).isFalse();
    }
    @Test
    @DisplayName("A temporary grant created before Archetype enters resumes when it leaves")
    void existingTemporaryGrantResumesAfterRestrictionLeaves() {
        Permanent opposingImp = addCreatureReady(player2, new FetidImp());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opposingImp, Keyword.DEATHTOUCH)).isTrue();

        Permanent archetype = addCreatureReady(player1, new ArchetypeOfFinality());
        assertThat(gqs.hasKeyword(gd, opposingImp, Keyword.DEATHTOUCH)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(archetype);

        assertThat(gqs.hasKeyword(gd, opposingImp, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Opposing Archetypes prevent deathtouch on both sides regardless of entry order")
    void opposingArchetypesPreventDeathtouchOnBothSides() {
        Permanent first = addCreatureReady(player1, new ArchetypeOfFinality());
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isTrue();

        Permanent second = addCreatureReady(player2, new ArchetypeOfFinality());
        Permanent laterOwnCreature = addCreatureReady(player1, new ArchetypeOfFinality());
        Permanent laterOpposingCreature = addCreatureReady(player2, new ArchetypeOfFinality());

        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterOwnCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterOpposingCreature, Keyword.DEATHTOUCH)).isFalse();

        gd.playerBattlefields.get(player1.getId()).clear();

        assertThat(gqs.hasKeyword(gd, second, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterOpposingCreature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Printed deathtouch returns and your granted deathtouch ends when Archetype leaves")
    void leavingRestoresPrintedDeathtouchAndEndsOwnGrant() {
        Permanent archetype = addCreatureReady(player1, new ArchetypeOfFinality());
        Permanent ownCreature = addCreatureReady(player1, new FetidImp());
        Permanent opposingCreature = addCreatureReady(player2, new BalefulEidolon());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.DEATHTOUCH)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(archetype);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.DEATHTOUCH)).isTrue();
    }
}
