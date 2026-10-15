package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiralingDuelist.class, Spellbook.class, LeoninScimitar.class, BottleGnomes.class})
class SpiralingDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("No double strike with zero artifacts")
    void noDoubleStrikeWithZeroArtifacts() {
        Permanent duelist = harness.addToBattlefieldAndReturn(player1, new SpiralingDuelist());
        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("No double strike with two artifacts")
    void noDoubleStrikeWithTwoArtifacts() {
        harness.addToBattlefield(player1, new SpiralingDuelist());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent duelist = findPermanent(player1, "Spiraling Duelist");
        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Has double strike with exactly three artifacts")
    void hasDoubleStrikeWithThreeArtifacts() {
        harness.addToBattlefield(player1, new SpiralingDuelist());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent duelist = findPermanent(player1, "Spiraling Duelist");
        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Loses double strike when artifact count drops below three")
    void losesDoubleStrikeWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new SpiralingDuelist());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent duelist = findPermanent(player1, "Spiraling Duelist");
        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isTrue();

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Bottle Gnomes"));
        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new SpiralingDuelist());
        // Opponent has 3 artifacts, controller has 0
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new BottleGnomes());

        Permanent duelist = findPermanent(player1, "Spiraling Duelist");
        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Gains double strike immediately when the third artifact enters")
    void gainsDoubleStrikeWhenThirdArtifactEnters() {
        Permanent duelist = harness.addToBattlefieldAndReturn(player1, new SpiralingDuelist());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.addToBattlefield(player1, new BottleGnomes());

        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Bottle Gnomes"), Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Unblocked Duelist deals damage twice with metalcraft")
    void dealsDoubleStrikeCombatDamageWithMetalcraft() {
        addCreatureReady(player1, new SpiralingDuelist());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Unblocked Duelist deals damage once without metalcraft")
    void dealsNormalCombatDamageWithoutMetalcraft() {
        addCreatureReady(player1, new SpiralingDuelist());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}
