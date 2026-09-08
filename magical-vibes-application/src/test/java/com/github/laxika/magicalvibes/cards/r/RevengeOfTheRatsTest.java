package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RevengeOfTheRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one tapped Rat for each creature card in your graveyard")
    void createsTappedRatsForCreatureCardsInControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RevengeOfTheRats()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> rats = ratTokens();
        assertThat(rats).hasSize(2).allSatisfy(rat -> {
            assertThat(rat.isTapped()).isTrue();
            assertThat(rat.getCard().getPower()).isEqualTo(1);
            assertThat(rat.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Flashback creates the Rats and exiles Revenge of the Rats")
    void flashbackCreatesRatsAndExilesSpell() {
        harness.setGraveyard(player1, List.of(
                new RevengeOfTheRats(), new GrizzlyBears(), new LlanowarElves(), new Shock()));
        addMana();

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(ratTokens()).hasSize(2);
        harness.assertNotInGraveyard(player1, "Revenge of the Rats");
        GameData gameData = harness.getGameData();
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Revenge of the Rats"));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Permanent> ratTokens() {
        return harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Rat"))
                .toList();
    }
}
