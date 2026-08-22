package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RooftopAssassin.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class RooftopAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an opponent's creature that was dealt damage this turn")
    void destroysDamagedOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new RooftopAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Rooftop Assassin");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature that was not dealt damage this turn")
    void cannotTargetUndamagedOpponentCreature() {
        Permanent damagedCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.getGameData().permanentsDealtDamageThisTurn.add(damagedCreature.getId());
        Permanent undamagedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RooftopAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, undamagedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Cannot target a damaged creature controlled by you")
    void cannotTargetOwnDamagedCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.getGameData().permanentsDealtDamageThisTurn.add(ownCreature.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RooftopAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }
}
