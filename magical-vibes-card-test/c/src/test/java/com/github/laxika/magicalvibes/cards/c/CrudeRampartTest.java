package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrudeRampart.class})
class CrudeRampartTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new CrudeRampart()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent rampart = findPermanent(player1, "Crude Rampart");
        assertThat(rampart.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int rampartIndex = gd.playerBattlefields.get(player1.getId()).indexOf(rampart);
        harness.turnFaceUp(player1, rampartIndex);
        harness.passBothPriorities();

        assertThat(rampart.isFaceDown()).isFalse();
    }

    @Test
    void cannotAttackBecauseItHasDefender() {
        addCreatureReady(player1, new CrudeRampart());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void faceDownRampartCanAttackAndTurningFaceUpDoesNotRemoveItFromCombat() {
        harness.setHand(player1, List.of(new CrudeRampart()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent rampart = findPermanent(player1, "Crude Rampart");
        rampart.setSummoningSick(false);
        assertThat(gqs.getEffectivePower(gd, rampart)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rampart)).isEqualTo(2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(rampart.isAttacking()).isTrue();
        assertThat(rampart.isTapped()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.turnFaceUp(player1, 0));

        assertThat(rampart.isFaceDown()).isFalse();
        assertThat(rampart.isAttacking()).isTrue();
        assertThat(rampart.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTurnFaceUpForItsCheaperPrintedManaCost() {
        harness.setHand(player1, List.of(new CrudeRampart()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(findPermanent(player1, "Crude Rampart").isFaceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    void turningFaceUpRequiresWhiteMana() {
        harness.setHand(player1, List.of(new CrudeRampart()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(findPermanent(player1, "Crude Rampart").isFaceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }
}
