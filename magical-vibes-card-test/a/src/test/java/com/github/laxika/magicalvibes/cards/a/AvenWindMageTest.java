package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenWindMage.class, Shock.class, Ponder.class, GrizzlyBears.class})
class AvenWindMageTest extends BaseCardTest {

    private Permanent addMage() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new AvenWindMage());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return mage;
    }

    @Test
    @DisplayName("Gets +1/+1 when you cast an instant")
    void instantSpellPumps() {
        Permanent mage = addMage();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(mage.getPowerModifier()).isEqualTo(1);
        assertThat(mage.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 when you cast a sorcery")
    void sorcerySpellPumps() {
        Permanent mage = addMage();

        harness.setHand(player1, List.of(new Ponder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(mage.getPowerModifier()).isEqualTo(1);
        assertThat(mage.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when you cast a creature spell")
    void creatureSpellDoesNotTrigger() {
        Permanent mage = addMage();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mage.getPowerModifier()).isEqualTo(0);
        assertThat(mage.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's instant does not pump the mage")
    void opponentInstantDoesNotTrigger() {
        Permanent mage = addMage();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(mage.getPowerModifier()).isZero();
        assertThat(mage.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each cast pumps the mage before the spell resolves and bonuses expire")
    void repeatedCastsAccumulateUntilEndOfTurn() {
        Permanent mage = addMage();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(mage.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(mage.getPowerModifier()).isEqualTo(1);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(mage.getPowerModifier()).isEqualTo(2);
        assertThat(mage.getToughnessModifier()).isEqualTo(2);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(mage.getPowerModifier()).isZero();
        assertThat(mage.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Controller's instant pumps the mage during the opponent's turn")
    void instantDuringOpponentTurnTriggers() {
        Permanent mage = addMage();
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(mage.getPowerModifier()).isEqualTo(1);
        assertThat(mage.getToughnessModifier()).isEqualTo(1);
    }
}
