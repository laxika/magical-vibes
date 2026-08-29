package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrawlingSensationTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger mills two cards")
    void upkeepMillsTwoCards() {
        harness.addToBattlefield(player1, new CrawlingSensation());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining the upkeep trigger does not mill")
    void decliningUpkeepDoesNotMill() {
        harness.addToBattlefield(player1, new CrawlingSensation());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling multiple lands creates only one Insect token per turn")
    void millingMultipleLandsCreatesOneInsectTokenPerTurn() {
        harness.addToBattlefield(player1, new CrawlingSensation());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(countInsectTokens(player1)).isEqualTo(1);
    }

    private long countInsectTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.INSECT))
                .count();
    }
}
