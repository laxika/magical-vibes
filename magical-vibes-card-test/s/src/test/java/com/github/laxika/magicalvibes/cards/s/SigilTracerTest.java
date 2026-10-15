package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.ConeOfFlame;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SigilTracer.class, Boomerang.class, CounselOfTheSoratami.class,
        FugitiveWizard.class, GrizzlyBears.class, ConeOfFlame.class})
class SigilTracerTest extends BaseCardTest {

    private int prepareTracer(int extraWizards) {
        Permanent tracer = addCreatureReady(player2, new SigilTracer());
        for (int i = 0; i < extraWizards; i++) {
            addCreatureReady(player2, new FugitiveWizard());
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

        int tracerIdx = prepareTracer(1);

        harness.castFromHand(player1, counsel, "{2}{U}");
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

        int tracerIdx = prepareTracer(1);

        harness.castFromHand(player1, counsel, "{2}{U}");
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

        int tracerIdx = prepareTracer(1);

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, tracerIdx, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a second untapped Wizard")
    void cannotActivateWithOnlyOneWizard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();

        int tracerIdx = prepareTracer(0);

        harness.castFromHand(player1, counsel, "{2}{U}");
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, tracerIdx, null, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copies an instant and can retarget the copy")
    void copiesAndRetargetsInstant() {
        Permanent originalTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent newTarget = addCreatureReady(player1, new GrizzlyBears());
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        int tracerIdx = prepareTracer(1);

        harness.castInstant(player1, 0, originalTarget.getId());
        harness.passPriority(player1);

        harness.activateAbility(player2, tracerIdx, null, boomerang.getId());
        tapWizards(2);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, newTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(originalTarget.getId()))
                .noneMatch(permanent -> permanent.getId().equals(newTarget.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(newTarget.getCard().getId()));
    }

    @Test
    @DisplayName("Summoning-sick Wizards, including Sigil Tracer, can pay the tap cost")
    void summoningSickWizardsCanPayCost() {
        Permanent tracer = harness.addToBattlefieldAndReturn(player2, new SigilTracer());
        Permanent wizard = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        tracer.setSummoningSick(true);
        wizard.setSummoningSick(true);
        harness.addMana(player2, ManaColor.BLUE, 2);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.castFromHand(player1, counsel, "{2}{U}");
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, counsel.getId());
        harness.handlePermanentChosen(player2, tracer.getId());
        harness.handlePermanentChosen(player2, wizard.getId());
        harness.passBothPriorities();

        assertThat(tracer.isTapped()).isTrue();
        assertThat(wizard.isTapped()).isTrue();
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    @DisplayName("Declining new targets keeps the original target and leaves the original spell unchanged")
    void canKeepOriginalTarget() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        int tracerIdx = prepareTracer(1);
        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, tracerIdx, null, boomerang.getId());
        tapWizards(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getId().equals(target.getCard().getId())).hasSize(1);
    }

    @Test
    @DisplayName("All three targets of a copied Cone of Flame can be changed")
    void canChangeEveryTargetOfMultiTargetCopy() {
        List<Permanent> originalTargets = List.of(
                addCreatureReady(player1, new GrizzlyBears()),
                addCreatureReady(player1, new GrizzlyBears()),
                addCreatureReady(player1, new GrizzlyBears()));
        List<Permanent> newTargets = List.of(
                addCreatureReady(player1, new GrizzlyBears()),
                addCreatureReady(player1, new GrizzlyBears()),
                addCreatureReady(player1, new GrizzlyBears()));
        ConeOfFlame cone = new ConeOfFlame();
        int tracerIdx = prepareTracer(1);
        harness.setHand(player1, List.of(cone));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, originalTargets.stream().map(Permanent::getId).toList());
        harness.passPriority(player1);
        harness.activateAbility(player2, tracerIdx, null, cone.getId());
        tapWizards(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        for (Permanent target : newTargets) {
            harness.handlePermanentChosen(player2, target.getId());
        }
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsAll(originalTargets)
                .contains(newTargets.getFirst())
                .doesNotContain(newTargets.get(1), newTargets.get(2));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactlyElementsOf(originalTargets.stream().map(Permanent::getId).toList());
    }
}
