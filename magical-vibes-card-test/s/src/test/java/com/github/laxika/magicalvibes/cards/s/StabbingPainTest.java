package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StabbingPain.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, Swamp.class})
class StabbingPainTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives -1/-1 and taps target creature")
    void resolvingGivesMinusAndTaps() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new StabbingPain()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveInstant(player1, 0, giantId);

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.getPowerModifier()).isEqualTo(-1);
        assertThat(giant.getToughnessModifier()).isEqualTo(-1);
        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature survives -1/-1 if toughness is high enough")
    void creatureSurvivesIfToughnessHighEnough() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StabbingPain()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Grizzly Bears is 2/2, -1/-1 makes it 1/1 — it survives
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StabbingPain()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Stabbing Pain goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new StabbingPain()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveInstant(player1, 0, giantId);

        harness.assertInGraveyard(player1, "Stabbing Pain");
    }

    @Test
    @DisplayName("One-toughness creature is tapped before dying from zero toughness")
    void tapsBeforeZeroToughnessCreatureDies() {
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new StabbingPain()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, elves.getId());

        assertThat(elves.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Already-tapped creature still gets -1/-1, including your own creature")
    void debuffsOwnAlreadyTappedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();
        harness.setHand(player1, List.of(new StabbingPain()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The -1/-1 expires at cleanup but tapping does not")
    void debuffExpiresAtCleanup() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StabbingPain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent despite the tapping instruction")
    void cannotTargetNoncreaturePermanent() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new StabbingPain()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
