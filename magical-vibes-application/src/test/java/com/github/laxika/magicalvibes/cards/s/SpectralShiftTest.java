package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpectralShiftTest extends BaseCardTest {

    private void castMode(int mode, UUID targetId) {
        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{mode}), targetId, null);
    }

    @Test
    @DisplayName("Changes a basic land type on a target permanent")
    void changesBasicLandType() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpectralShift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        castMode(0, target.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "PLAINS");

        assertThat(target.getTextReplacements())
                .containsExactly(new TextReplacement("Swamp", "Plains"));
    }

    @Test
    @DisplayName("Changes a color word on a target permanent")
    void changesColorWord() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new SpectralShift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        castMode(1, target.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(target.getTextReplacements())
                .containsExactly(new TextReplacement("red", "green"));
    }

    @Test
    @DisplayName("Changes a target spell's basic land type before it becomes a permanent")
    void changesBasicLandTypeOnTargetSpell() {
        harness.setHand(player1, List.of(new SpectralShift(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 1);
        UUID spellId = gd.stack.getFirst().getCard().getId();
        castMode(0, spellId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "PLAINS");
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Grizzly Bears");
        assertThat(target.getTextReplacements())
                .containsExactly(new TextReplacement("Swamp", "Plains"));
    }

    @Test
    @DisplayName("Entwine applies both text changes to the same target and pays the additional cost")
    void entwineAppliesBothModes() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new SpectralShift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}),
                target.getId(), null, List.of(target.getId()), List.of());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "PLAINS");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(target.getTextReplacements()).containsExactly(
                new TextReplacement("Swamp", "Plains"),
                new TextReplacement("red", "green"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SpectralShift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> castMode(0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
