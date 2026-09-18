package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuriokChampion;
import com.github.laxika.magicalvibes.cards.v.VedalkenShackles;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectralShift.class, AuriokChampion.class, SylvokExplorer.class, VedalkenShackles.class})
class SpectralShiftTest extends BaseCardTest {

    private void castMode(int mode, UUID targetId) {
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{mode}, targetId, List.of());
    }

    @Test
    @DisplayName("Changes a basic land type on a target permanent")
    void changesBasicLandType() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VedalkenShackles());
        harness.setHand(player1, List.of(new SpectralShift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        castMode(0, target.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "ISLAND");
        harness.handleListChoice(player1, "SWAMP");

        assertThat(target.getTextReplacements())
                .containsExactly(new TextReplacement("Island", "Swamp"));
    }

    @Test
    @DisplayName("Changes a color word on a target permanent")
    void changesColorWord() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AuriokChampion());
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
        harness.setHand(player1, List.of(new SpectralShift(), new VedalkenShackles()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 1);
        UUID spellId = gd.stack.getFirst().getCard().getId();
        castMode(0, spellId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "ISLAND");
        harness.handleListChoice(player1, "SWAMP");
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Vedalken Shackles");
        assertThat(target.getTextReplacements())
                .containsExactly(new TextReplacement("Island", "Swamp"));
    }

    @Test
    @DisplayName("Changes a color word on a target spell before it becomes a permanent")
    void changesColorWordOnTargetSpell() {
        Permanent greenSource = harness.addToBattlefieldAndReturn(player2, new SylvokExplorer());
        harness.setHand(player1, List.of(new SpectralShift(), new AuriokChampion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 1);
        UUID spellId = gd.stack.getFirst().getCard().getId();
        castMode(1, spellId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Auriok Champion");
        assertThat(gqs.hasProtectionFromSource(gd, target, greenSource)).isTrue();
    }

    @Test
    @DisplayName("Entwine applies both text changes to the same target and pays the additional cost")
    void entwineAppliesBothModes() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AuriokChampion());
        harness.setHand(player1, List.of(new SpectralShift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                target.getId(), List.of(target.getId()));
        harness.passBothPriorities();

        harness.handleListChoice(player1, "ISLAND");
        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(target.getTextReplacements()).containsExactly(
                new TextReplacement("Island", "Swamp"),
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
