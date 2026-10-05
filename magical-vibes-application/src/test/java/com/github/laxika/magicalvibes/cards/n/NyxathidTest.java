package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nyxathid.class, GrizzlyBears.class, Persuasion.class})
class NyxathidTest extends BaseCardTest {

    @Test
    @DisplayName("Full 7/7 when the opponent's hand is empty")
    void fullSizeWithEmptyOpponentHand() {
        harness.setHand(player2, List.of());
        Permanent nyxathid = addNyxathid(player1);

        assertThat(gqs.getEffectivePower(gd, nyxathid)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, nyxathid)).isEqualTo(7);
    }

    @Test
    @DisplayName("Gets -1/-1 for each card in the opponent's hand")
    void shrinksWithOpponentHandSize() {
        harness.setHand(player2, handOf(3));
        Permanent nyxathid = addNyxathid(player1);

        assertThat(gqs.getEffectivePower(gd, nyxathid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nyxathid)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts only the opponent's hand, not the controller's own")
    void ignoresControllerHand() {
        harness.setHand(player1, handOf(5));
        harness.setHand(player2, handOf(2));
        Permanent nyxathid = addNyxathid(player1);

        assertThat(gqs.getEffectivePower(gd, nyxathid)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, nyxathid)).isEqualTo(5);
    }

    @Test
    @DisplayName("Updates dynamically as the opponent's hand changes")
    void updatesDynamically() {
        harness.setHand(player2, handOf(2));
        Permanent nyxathid = addNyxathid(player1);
        assertThat(gqs.getEffectiveToughness(gd, nyxathid)).isEqualTo(5);

        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectiveToughness(gd, nyxathid)).isEqualTo(4);
    }

    @Test
    @DisplayName("Dies when reduced to 0 toughness by a seven-card opponent hand")
    void diesWhenReducedToZeroToughness() {
        harness.setHand(player2, handOf(7));
        addNyxathid(player1);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Nyxathid");
        harness.assertInGraveyard(player1, "Nyxathid");
    }

    @Test
    @DisplayName("Still counts the chosen player's hand after that player gains control")
    void keepsChosenPlayerAfterControlChanges() {
        harness.setHand(player1, handOf(3));
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Nyxathid(), "{1}{B}{B}");
        harness.passBothPriorities();
        Permanent nyxathid = findPermanent(player2, "Nyxathid");
        assertThat(gqs.getEffectiveToughness(gd, nyxathid)).isEqualTo(4);

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Persuasion(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, nyxathid.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nyxathid");
        assertThat(gqs.getEffectivePower(gd, nyxathid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nyxathid)).isEqualTo(4);
    }

    private Permanent addNyxathid(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Nyxathid());
    }

    private List<Card> handOf(int count) {
        return new ArrayList<>(IntStream.range(0, count)
                .mapToObj(i -> (Card) new GrizzlyBears())
                .toList());
    }
}
