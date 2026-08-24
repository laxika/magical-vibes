package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MorkrutBehemoth.class, GrizzlyBears.class})
class MorkrutBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature as an additional cost")
    void sacrificesCreatureAsAdditionalCost() {
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        harness.setHand(player1, List.of(new MorkrutBehemoth()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Morkrut Behemoth");
    }

    @Test
    @DisplayName("Pays {1}{B} instead of sacrificing")
    void paysManaInsteadOfSacrificing() {
        harness.setHand(player1, List.of(new MorkrutBehemoth()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Morkrut Behemoth");
    }

    @Test
    @DisplayName("Cannot cast without a creature or enough mana for the additional cost")
    void cannotCastWithoutCreatureOrMana() {
        harness.setHand(player1, List.of(new MorkrutBehemoth()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }
}
