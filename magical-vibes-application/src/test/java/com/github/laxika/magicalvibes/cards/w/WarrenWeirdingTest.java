package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarrenWeirdingTest extends BaseCardTest {

    private void castAtPlayer2() {
        harness.setHand(player1, new ArrayList<>(List.of(new WarrenWeirding())));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    private long goblinRogueCount(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Rogue"))
                .count();
    }

    @Test
    @DisplayName("Target player sacrifices their only creature (a Goblin) and creates two hasty Goblin Rogue tokens")
    void goblinSacrificedCreatesTokens() {
        addCreatureReady(player2, new GoblinPiker());

        castAtPlayer2();

        // The Goblin was sacrificed.
        harness.assertNotOnBattlefield(player2, "Goblin Piker");
        harness.assertInGraveyard(player2, "Goblin Piker");

        // The sacrificing player created two Goblin Rogue tokens...
        List<Permanent> tokens = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Rogue"))
                .toList();
        assertThat(tokens).hasSize(2);

        // ...with haste, under the target player's control (not the caster's).
        for (Permanent token : tokens) {
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        }
        assertThat(goblinRogueCount(player1)).isZero();
    }

    @Test
    @DisplayName("Non-Goblin sacrifice does not create tokens")
    void nonGoblinSacrificeCreatesNoTokens() {
        addCreatureReady(player2, new GrizzlyBears());

        castAtPlayer2();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(goblinRogueCount(player2)).isZero();
    }

    @Test
    @DisplayName("With multiple creatures, target player chooses which to sacrifice; choosing the Goblin creates tokens")
    void choosingGoblinAmongCreaturesCreatesTokens() {
        Permanent goblin = addCreatureReady(player2, new GoblinPiker());
        addCreatureReady(player2, new GrizzlyBears());

        castAtPlayer2();

        // Resolution pauses for the target player's sacrifice choice.
        harness.handlePermanentChosen(player2, goblin.getId());

        harness.assertNotOnBattlefield(player2, "Goblin Piker");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(goblinRogueCount(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("With multiple creatures, choosing a non-Goblin creates no tokens")
    void choosingNonGoblinCreatesNoTokens() {
        addCreatureReady(player2, new GoblinPiker());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castAtPlayer2();

        harness.handlePermanentChosen(player2, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Goblin Piker");
        assertThat(goblinRogueCount(player2)).isZero();
    }

    @Test
    @DisplayName("Target player with no creatures sacrifices nothing and creates no tokens")
    void noCreaturesNoEffect() {
        castAtPlayer2();

        assertThat(goblinRogueCount(player2)).isZero();
    }
}
