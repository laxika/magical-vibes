package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EsquireOfTheKing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BardHeirOfGirion.class, EsquireOfTheKing.class})
class BardHeirOfGirionTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other creatures you control")
    void boostsOtherCreaturesYouControl() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new BardHeirOfGirion());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new EsquireOfTheKing());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new EsquireOfTheKing());

        assertThat(gqs.getEffectivePower(gd, bard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bard)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws a card when you attack")
    void drawsWhenYouAttack() {
        harness.addToBattlefield(player1, new BardHeirOfGirion());
        Permanent attacker = addCreatureReady(player1, new EsquireOfTheKing());
        Card drawn = new EsquireOfTheKing();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws only once when multiple creatures attack, including Bard")
    void drawsOnceForMultipleAttackers() {
        Permanent bard = addCreatureReady(player1, new BardHeirOfGirion());
        addCreatureReady(player1, new EsquireOfTheKing());
        addCreatureReady(player1, new EsquireOfTheKing());
        Card drawn = new EsquireOfTheKing();
        Card remaining = new EsquireOfTheKing();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn, remaining));

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(bard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not draw when no creatures attack")
    void doesNotDrawWithoutAttackers() {
        addCreatureReady(player1, new BardHeirOfGirion());
        Card remaining = new EsquireOfTheKing();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(remaining));

        declareAttackers(List.of());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Does not draw when an opponent attacks")
    void doesNotDrawForOpponentAttack() {
        harness.addToBattlefield(player1, new BardHeirOfGirion());
        addCreatureReady(player2, new EsquireOfTheKing());
        Card remaining = new EsquireOfTheKing();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(remaining));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }
}
