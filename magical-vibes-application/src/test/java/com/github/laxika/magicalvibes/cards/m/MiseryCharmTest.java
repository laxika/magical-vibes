package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NovaCleric;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MiseryCharm.class, NovaCleric.class, GrizzlyBears.class})
class MiseryCharmTest extends BaseCardTest {

    @Test
    void destroysTargetCleric() {
        harness.addToBattlefield(player2, new NovaCleric());
        harness.setHand(player1, List.of(new MiseryCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Nova Cleric"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nova Cleric");
    }

    @Test
    void cannotDestroyNonCleric() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MiseryCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsTargetClericFromGraveyard() {
        Card cleric = new NovaCleric();
        harness.setGraveyard(player1, List.of(cleric));
        harness.setHand(player1, List.of(new MiseryCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, 1, cleric.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(cleric);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(cleric);
    }

    @Test
    void cannotReturnNonClericFromGraveyard() {
        Card nonCleric = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonCleric));
        harness.setHand(player1, List.of(new MiseryCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, nonCleric.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void makesTargetPlayerLoseTwoLife() {
        harness.setHand(player1, List.of(new MiseryCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
