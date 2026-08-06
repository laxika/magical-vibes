package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimicCharmTest extends BaseCardTest {

    // Mode indices: 0 = target creature gets +3/+3, 1 = permanents you control gain hexproof,
    //               2 = return target creature to its owner's hand.

    @Test
    @DisplayName("Mode 0 gives the target creature +3/+3 until end of turn")
    void modeZeroBoostsCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SimicCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mode 1 makes your creature untargetable by an opponent's removal")
    void modeOneGrantsHexproof() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SimicCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.HEXPROOF)).isTrue();

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mode 1 hexproof wears off at end of turn")
    void modeOneWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SimicCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Mode 2 returns the target creature to its owner's hand")
    void modeTwoBouncesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SimicCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, 2, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> "Grizzly Bears".equals(card.getName()));
    }

    @Test
    @DisplayName("Mode 0 cannot target a noncreature permanent")
    void modeZeroRejectsNoncreatureTarget() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new SimicCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, spellbookId))
                .hasMessageContaining("creature");
    }
}
