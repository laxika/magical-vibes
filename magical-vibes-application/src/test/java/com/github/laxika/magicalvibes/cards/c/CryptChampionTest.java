package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CryptChampion.class, GrizzlyBears.class, HillGiant.class})
class CryptChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one qualifying creature from each graveyard and survives when red mana was spent")
    void returnsQualifyingCreatureFromEachGraveyardAndSurvivesWithRedMana() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(ownCreature, tooExpensive));
        harness.setGraveyard(player2, List.of(opponentCreature));

        castCryptChampion(true);
        resolveEnterTriggers();

        assertThat(battlefieldCards(player1)).contains(ownCreature);
        assertThat(battlefieldCards(player2)).contains(opponentCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(tooExpensive);
        harness.assertOnBattlefield(player1, "Crypt Champion");
    }

    @Test
    @DisplayName("Returns qualifying creatures before sacrificing itself when red mana was not spent")
    void returnsQualifyingCreaturesThenSacrificesWithoutRedMana() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(ownCreature, tooExpensive));
        harness.setGraveyard(player2, List.of(opponentCreature));

        castCryptChampion(false);
        resolveEnterTriggers();

        assertThat(battlefieldCards(player1)).contains(ownCreature);
        assertThat(battlefieldCards(player2)).contains(opponentCreature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(tooExpensive)
                .doesNotContain(ownCreature);
        harness.assertNotOnBattlefield(player1, "Crypt Champion");
        harness.assertInGraveyard(player1, "Crypt Champion");
    }

    private void castCryptChampion(boolean spendRedMana) {
        harness.setHand(player1, List.of(new CryptChampion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, spendRedMana ? 2 : 3);
        if (spendRedMana) {
            harness.addMana(player1, ManaColor.RED, 1);
        }
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void resolveEnterTriggers() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Card> battlefieldCards(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .map(permanent -> permanent.getCard())
                .toList();
    }
}
