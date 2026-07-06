package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PoisonersApprenticeTest extends BaseCardTest {

    

    @Test
    @DisplayName("With life gained, the ETB gives an opponent's creature -4/-4 (killing a 3/3)")
    void withLifeGainWeakensOpponentCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new PoisonersApprentice()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 1);

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        gs.playCard(gd, player1, 0, 0, targetId, null);
        harness.passBothPriorities(); // resolve creature -> ETB on stack
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Without life gained, the ETB does nothing and the creature survives")
    void withoutLifeGainDoesNothing() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new PoisonersApprentice()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        gs.playCard(gd, player1, 0, 0, targetId, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Cannot target your own creature")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new PoisonersApprentice()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID ownGiant = harness.getPermanentId(player1, "Hill Giant");

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownGiant, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }
}
