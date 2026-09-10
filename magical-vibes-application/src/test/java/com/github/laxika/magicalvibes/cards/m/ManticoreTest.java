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

@CardUsed({Manticore.class, GrizzlyBears.class})
class ManticoreTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a damaged creature an opponent controls when it enters")
    void destroysDamagedOpponentsCreatureOnEnter() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.permanentsDealtDamageThisTurn.add(bears.getId());

        castManticore(bears);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature that was not dealt damage this turn")
    void cannotTargetUndamagedOpponentsCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new Manticore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a damaged creature you control")
    void cannotTargetOwnDamagedCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        gd.permanentsDealtDamageThisTurn.add(bears.getId());

        harness.setHand(player1, List.of(new Manticore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castManticore(Permanent target) {
        harness.setHand(player1, List.of(new Manticore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
