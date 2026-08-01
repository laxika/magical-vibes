package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AerialPredationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the targeted flier and its controller's caster gains 2 life")
    void destroysFlierAndGainsLife() {
        Permanent airElemental = new Permanent(new AirElemental());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(airElemental);

        harness.setHand(player1, List.of(new AerialPredation()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, airElemental.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        harness.getGameData().playerBattlefields.get(player1.getId()).add(new Permanent(new AirElemental()));

        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new AerialPredation()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Fizzles with no life gain if the target leaves the battlefield")
    void fizzlesIfTargetRemoved() {
        Permanent airElemental = new Permanent(new AirElemental());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(airElemental);

        harness.setHand(player1, List.of(new AerialPredation()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, airElemental.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Aerial Predation");
    }
}
