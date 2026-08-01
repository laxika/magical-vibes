package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlistercoilWeirdTest extends BaseCardTest {

    private Permanent addWeird() {
        harness.addToBattlefield(player1, new BlistercoilWeird());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    @DisplayName("Gets +1/+1 and untaps when you cast an instant")
    void instantSpellPumpsAndUntaps() {
        Permanent weird = addWeird();
        weird.tap();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(1);
        assertThat(weird.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 when you cast a sorcery")
    void sorcerySpellPumps() {
        Permanent weird = addWeird();

        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(1);
        assertThat(weird.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when you cast a creature spell")
    void creatureSpellDoesNotTrigger() {
        Permanent weird = addWeird();
        weird.tap();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(weird.getPowerModifier()).isEqualTo(0);
        assertThat(weird.getToughnessModifier()).isEqualTo(0);
        assertThat(weird.isTapped()).isTrue();
    }
}
