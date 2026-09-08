package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HonoredHeirloom.class, GrizzlyBears.class, HillGiant.class})
class HonoredHeirloomTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for any color adds the chosen mana")
    void tapsForAnyColor() {
        harness.addToBattlefield(player1, new HonoredHeirloom());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiles a target card from a graveyard")
    void exilesTargetCardFromGraveyard() {
        Card target = new HillGiant();
        harness.addToBattlefield(player1, new HonoredHeirloom());
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }
}
