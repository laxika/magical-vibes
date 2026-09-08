package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GeralfsMindcrusher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrawlingInfestation.class, Forest.class, GeralfsMindcrusher.class, GrizzlyBears.class})
class CrawlingInfestationTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger mills two cards")
    void upkeepMillsTwoCards() {
        harness.addToBattlefield(player1, new CrawlingInfestation());
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
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling multiple creature cards creates only one Insect token per turn")
    void millingMultipleCreaturesCreatesOneInsectTokenPerTurn() {
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(countInsectTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature cards put into your graveyard during an opponent's turn do not trigger")
    void opponentTurnGraveyardEventDoesNotTrigger() {
        harness.addToBattlefield(player1, new CrawlingInfestation());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GeralfsMindcrusher()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castCreature(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countInsectTokens(player1)).isZero();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(countInsectTokens(player1)).isEqualTo(1);
    }

    private long countInsectTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.INSECT))
                .count();
    }
}
