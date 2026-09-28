package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CoralhelmCommander;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeeprootHistorian.class, CoralhelmCommander.class, Forest.class, GrizzlyBears.class})
class DeeprootHistorianTest extends BaseCardTest {

    @Test
    @DisplayName("Grants retrace to Merfolk cards in your graveyard")
    void grantsRetraceToMerfolkCards() {
        harness.addToBattlefield(player1, new DeeprootHistorian());
        harness.setGraveyard(player1, List.of(new CoralhelmCommander()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Coralhelm Commander"));
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Does not grant retrace to cards without a Merfolk or Druid subtype")
    void doesNotGrantRetraceToOtherCards() {
        harness.addToBattlefield(player1, new DeeprootHistorian());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
