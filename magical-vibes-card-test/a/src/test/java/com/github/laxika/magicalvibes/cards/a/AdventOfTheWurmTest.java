package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdventOfTheWurm.class})
class AdventOfTheWurmTest extends BaseCardTest {

    private static final int GREEN_MANA_NEEDED = 3;

    @Test
    @DisplayName("Creates a 5/5 green Wurm token with trample")
    void createsWurmToken() {
        harness.setHand(player1, List.of(new AdventOfTheWurm()));
        harness.addMana(player1, ManaColor.GREEN, GREEN_MANA_NEEDED);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        Permanent token = findPermanent(player1, "Wurm");

        assertThat(token.getCard().getPower()).isEqualTo(5);
        assertThat(token.getCard().getToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(harness.getGameData(), token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creates exactly one untapped green Wurm creature only on resolution")
    void createsTokenOnlyOnResolution() {
        harness.setHand(player1, List.of(new AdventOfTheWurm()));
        harness.addMana(player1, ManaColor.GREEN, GREEN_MANA_NEEDED);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent token = findPermanent(player1, "Wurm");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WURM);
        assertThat(token.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Advent of the Wurm");
    }

    @Test
    @DisplayName("Opponent casting the instant creates the token on their battlefield")
    void opponentCreatesTheirOwnToken() {
        harness.setHand(player2, List.of(new AdventOfTheWurm()));
        harness.addMana(player2, ManaColor.GREEN, GREEN_MANA_NEEDED);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        Permanent token = findPermanent(player2, "Wurm");
        assertThat(token.getCard().getPower()).isEqualTo(5);
        assertThat(token.getCard().getToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }
}
