package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PyrrhicRevivalTest extends BaseCardTest {

    private void castRevival() {
        harness.setHand(player1, List.of(new PyrrhicRevival()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each player returns each creature card from their graveyard with a -1/-1 counter")
    void returnsEachCreatureWithMinusOneCounter() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        castRevival();

        GameData gd = harness.getGameData();

        for (var pid : List.of(player1.getId(), player2.getId())) {
            Permanent bear = gd.playerBattlefields.get(pid).stream()
                    .filter(p -> p.getCard().hasType(CardType.CREATURE))
                    .findFirst().orElse(null);
            assertThat(bear).isNotNull();
            assertThat(bear.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
            assertThat(gd.playerGraveyards.get(pid)).noneMatch(c -> c.hasType(CardType.CREATURE));
        }
    }

    @Test
    @DisplayName("A 1/1 returned with a -1/-1 counter is a 0/0 and dies to state-based actions")
    void zeroToughnessCreatureDies() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(new FugitiveWizard())));

        castRevival();

        GameData gd = harness.getGameData();

        // The 0/0 wizard should not survive on the battlefield.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.CREATURE));
        // It ends up back in the graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.hasType(CardType.CREATURE));
    }

    @Test
    @DisplayName("Only creature cards are returned; noncreature cards stay in the graveyard")
    void onlyCreatureCardsAreReturned() {
        Card land = new Island();
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears(), land)));

        castRevival();

        GameData gd = harness.getGameData();

        long creaturesOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .count();
        assertThat(creaturesOnBf).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(land.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.hasType(CardType.CREATURE));
    }
}
