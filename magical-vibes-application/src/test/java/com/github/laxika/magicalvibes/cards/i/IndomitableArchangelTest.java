package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IndomitableArchangelTest extends BaseCardTest {

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Without metalcraft, artifacts do not have shroud")
    void noShroudWithoutMetalcraft() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent spellbook = findPermanent(player1, "Spellbook");

        assertThat(gqs.hasKeyword(gd, spellbook, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("With only two artifacts, artifacts do not have shroud")
    void noShroudWithTwoArtifacts() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent spellbook = findPermanent(player1, "Spellbook");

        assertThat(gqs.hasKeyword(gd, spellbook, Keyword.SHROUD)).isFalse();
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("With metalcraft, artifacts you control have shroud")
    void artifactsHaveShroudWithMetalcraft() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent spellbook = findPermanent(player1, "Spellbook");
        Permanent scimitar = findPermanent(player1, "Leonin Scimitar");
        Permanent gnomes = findPermanent(player1, "Bottle Gnomes");

        assertThat(gqs.hasKeyword(gd, spellbook, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, scimitar, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, gnomes, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Indomitable Archangel itself does not get shroud (not an artifact)")
    void archangelDoesNotGetShroud() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent archangel = findPermanent(player1, "Indomitable Archangel");

        assertThat(gqs.hasKeyword(gd, archangel, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Non-artifact creatures do not get shroud")
    void nonArtifactCreatureDoesNotGetShroud() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();
    }

    // ===== Opponent's artifacts =====

    @Test
    @DisplayName("Opponent's artifacts do not get shroud")
    void opponentArtifactsDoNotGetShroud() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        harness.addToBattlefield(player2, new Spellbook());

        Permanent opponentSpellbook = findPermanent(player2, "Spellbook");

        assertThat(gqs.hasKeyword(gd, opponentSpellbook, Keyword.SHROUD)).isFalse();
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Shroud is lost when artifact count drops below three")
    void shroudLostWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent spellbook = findPermanent(player1, "Spellbook");

        // With 3 artifacts, has shroud
        assertThat(gqs.hasKeyword(gd, spellbook, Keyword.SHROUD)).isTrue();

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Bottle Gnomes"));

        assertThat(gqs.hasKeyword(gd, spellbook, Keyword.SHROUD)).isFalse();
    }

    // ===== Opponent's artifacts don't count for metalcraft =====

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCountForMetalcraft() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        harness.addToBattlefield(player1, new Spellbook());
        // Only 1 artifact controlled by player1; 2 more on opponent's side
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new BottleGnomes());

        Permanent spellbook = findPermanent(player1, "Spellbook");

        assertThat(gqs.hasKeyword(gd, spellbook, Keyword.SHROUD)).isFalse();
    }
}
