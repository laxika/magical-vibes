package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MilitantAngel.class})
class MilitantAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Knight per distinct opponent attacked this turn")
    void createsOneKnightPerOpponentAttacked() {
        addCreatureReady(player1, new MilitantAngel());
        addCreatureReady(player1, new MilitantAngel());
        declareAttackers(List.of(0, 1));
        resolveCombat();

        castMilitantAngel(player1);

        assertThat(findPermanents(player1, "Knight")).hasSize(1);
    }

    @Test
    @DisplayName("Creates no Knights when you did not attack this turn")
    void createsNoKnightsWithoutAttacking() {
        castMilitantAngel(player1);

        assertThat(findPermanents(player1, "Knight")).isEmpty();
    }

    @Test
    @DisplayName("Attacks from a previous turn do not create Knights")
    void ignoresAttacksFromPreviousTurn() {
        addCreatureReady(player1, new MilitantAngel());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        castMilitantAngel(player1);

        assertThat(findPermanents(player1, "Knight")).isEmpty();
    }

    @Test
    @DisplayName("Created Knights deal two damage and do not tap to attack")
    void createdKnightsHaveVigilanceAndDealTwoDamage() {
        addCreatureReady(player1, new MilitantAngel());
        declareAttackers(List.of(0));
        resolveCombat();
        castMilitantAngel(player1);
        var knight = findPermanent(player1, "Knight");
        assertThat(knight.isTapped()).isFalse();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player2, 20);
        int knightIndex = gd.playerBattlefields.get(player1.getId()).indexOf(knight);
        declareAttackers(List.of(knightIndex));
        resolveCombat();

        assertThat(knight.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
    private void castMilitantAngel(Player player) {
        harness.setHand(player, List.of(new MilitantAngel()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreature(player, 0);
        resolveAllTriggers();
    }
}
