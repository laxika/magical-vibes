package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InnocuousInsect.class, GrizzlyBears.class})
class InnocuousInsectTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Innocuous Insect draws a card before it enters the battlefield")
    void castingDrawsCard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new InnocuousInsect()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Innocuous Insect"));
    }

    @Test
    @DisplayName("Paying buyback returns Innocuous Insect to its owner's hand")
    void buybackReturnsToHand() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        InnocuousInsect insect = new InnocuousInsect();
        harness.setHand(player1, List.of(insect));
        addMana();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithBuyback(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(insect);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(insect);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
