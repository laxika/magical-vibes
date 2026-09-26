package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BardHeirOfGirion.class, GrizzlyBears.class})
class BardHeirOfGirionTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other creatures you control")
    void boostsOtherCreaturesYouControl() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new BardHeirOfGirion());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bard)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws a card when you attack")
    void drawsWhenYouAttack() {
        harness.addToBattlefield(player1, new BardHeirOfGirion());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Card drawn = new Card();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
