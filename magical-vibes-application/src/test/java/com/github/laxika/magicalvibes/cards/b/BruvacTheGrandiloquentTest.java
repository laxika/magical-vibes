package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BruvacTheGrandiloquent.class, Millstone.class, Forest.class})
class BruvacTheGrandiloquentTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles an opponent's mill")
    void doublesOpponentMill() {
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.addToBattlefield(player1, new BruvacTheGrandiloquent());
        millstone.setSummoningSick(false);
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not double its controller's mill")
    void doesNotDoubleControllerMill() {
        Permanent millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.addToBattlefield(player1, new BruvacTheGrandiloquent());
        millstone.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
