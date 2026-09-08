package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SprintingWarbrute.class)
class SprintingWarbruteTest extends BaseCardTest {

    @Test
    @DisplayName("Sprinting Warbrute must attack when able")
    void mustAttackWhenAble() {
        Permanent warbrute = new Permanent(new SprintingWarbrute());
        warbrute.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(warbrute);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Normal cast does not grant haste or return the creature at end step")
    void normalCastDoesNotUseDash() {
        harness.setHand(player1, List.of(new SprintingWarbrute()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent warbrute = findPermanent(player1, "Sprinting Warbrute");
        assertThat(warbrute.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sprinting Warbrute")).isSameAs(warbrute);
    }

    @Test
    @DisplayName("Dash grants haste and returns the creature to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new SprintingWarbrute()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent warbrute = findPermanent(player1, "Sprinting Warbrute");
        assertThat(warbrute.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sprinting Warbrute");
        harness.assertNotOnBattlefield(player1, "Sprinting Warbrute");
    }
}
