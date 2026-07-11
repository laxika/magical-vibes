package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SigilTracerTest extends BaseCardTest {

    private int prepareTracer(int extraWizards) {
        Permanent tracer = new Permanent(new SigilTracer());
        tracer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(tracer);
        for (int i = 0; i < extraWizards; i++) {
            Permanent wiz = new Permanent(new FugitiveWizard());
            wiz.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(wiz);
        }
        harness.addMana(player2, ManaColor.BLUE, 2);
        return gd.playerBattlefields.get(player2.getId()).indexOf(tracer);
    }

    private void tapWizards(int count) {
        List<Permanent> wizards = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> !p.isTapped())
                .filter(p -> p.getCard().getName().equals("Sigil Tracer")
                        || p.getCard().getName().equals("Fugitive Wizard"))
                .limit(count)
                .toList();
        for (Permanent w : wizards) {
            harness.handlePermanentChosen(player2, w.getId());
        }
    }

    @Test
    @DisplayName("Copies target sorcery onto the stack and taps two Wizards")
    void copiesTargetSorcery() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int tracerIdx = prepareTracer(1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        harness.activateAbility(player2, tracerIdx, null, counsel.getId());
        tapWizards(2);

        // Resolve the ability — it creates the copy on the stack
        harness.passBothPriorities();

        // Copy is on the stack above the original
        StackEntry copy = gd.stack.getLast();
        assertThat(copy.isCopy()).isTrue();
        assertThat(copy.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(copy.getControllerId()).isEqualTo(player2.getId());

        // Two Wizards were tapped to pay the cost
        long tappedWizards = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(Permanent::isTapped)
                .filter(p -> p.getCard().getName().equals("Sigil Tracer")
                        || p.getCard().getName().equals("Fugitive Wizard"))
                .count();
        assertThat(tappedWizards).isEqualTo(2);
    }

    @Test
    @DisplayName("Copy of a draw sorcery makes the ability's controller draw")
    void copyDrawsForController() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int tracerIdx = prepareTracer(1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player2, tracerIdx, null, counsel.getId());
        tapWizards(2);

        // Resolve the ability (creates the copy), then resolve the copy — player2 draws 2
        harness.passBothPriorities();
        harness.passBothPriorities();

        int p2HandAfter = gd.playerHands.get(player2.getId()).size();
        assertThat(p2HandAfter - p2HandBefore).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int tracerIdx = prepareTracer(1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, tracerIdx, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a second untapped Wizard")
    void cannotActivateWithOnlyOneWizard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int tracerIdx = prepareTracer(0);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, tracerIdx, null, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
