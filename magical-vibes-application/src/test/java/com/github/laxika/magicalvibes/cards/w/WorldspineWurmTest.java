package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorldspineWurmTest extends BaseCardTest {

    @Test
    @DisplayName("When Worldspine Wurm dies it creates three 5/5 trampling Wurm tokens")
    void deathCreatesThreeWurmTokens() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new WorldspineWurm());
        wurm.setMarkedDamage(15);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wurm"))
                .toList();

        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(5);
            assertThat(token.getCard().getToughness()).isEqualTo(5);
            assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE);
        });
    }

    @Test
    @DisplayName("When Worldspine Wurm is put into a graveyard it is shuffled into its owner's library")
    void deathShufflesItselfIntoLibrary() {
        harness.setLibrary(player1, new ArrayList<>());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new WorldspineWurm());
        wurm.setMarkedDamage(15);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Worldspine Wurm");
        harness.assertInGraveyard(player1, "Worldspine Wurm");

        // Death creates tokens and the from-anywhere shuffle both trigger; resolve both.
        resolveAllTriggers();

        harness.assertNotInGraveyard(player1, "Worldspine Wurm");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Worldspine Wurm"));
    }
}
