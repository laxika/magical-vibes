package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheChiefWarg.class, AirElemental.class, GrizzlyBears.class})
class TheChiefWargTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and loses 1 life when attacking while controlling a creature with power 4 or greater")
    void drawsAndLosesLifeForFerociousAttack() {
        addCreatureReady(player1, new TheChiefWarg());
        addCreatureReady(player1, new AirElemental());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card(), new Card()));
        int lifeBeforeAttack = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBeforeAttack - 1);
    }

    @Test
    @DisplayName("Does not trigger without a creature with power 4 or greater")
    void doesNotTriggerWithoutQualifyingCreature() {
        addCreatureReady(player1, new TheChiefWarg());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));
        int lifeBeforeAttack = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBeforeAttack);
    }

    @Test
    @DisplayName("Does not trigger from an opponent's qualifying creature")
    void doesNotTriggerFromOpponentsCreature() {
        addCreatureReady(player1, new TheChiefWarg());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new AirElemental());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));
        int lifeBeforeAttack = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBeforeAttack);
    }
}
