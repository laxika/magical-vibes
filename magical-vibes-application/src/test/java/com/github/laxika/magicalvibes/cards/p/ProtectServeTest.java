package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MazeRusher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Protect // Serve is one card whose two halves (and their fusion) are the three modes of a single
 * modal instant, each paying its own total cost.
 */
@CardUsed({ProtectServe.class, GrizzlyBears.class, MazeRusher.class, PossibilityStorm.class})
class ProtectServeTest extends BaseCardTest {

    private static final int PROTECT = 0;
    private static final int SERVE = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Protect pumps the targeted creature +2/+4")
    void protectPumpsTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ProtectServe()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, PROTECT, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Serve weakens the targeted creature -6/-0")
    void serveWeakensTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ProtectServe()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, SERVE, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(-4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Serve is castable off blue mana alone — the mode's {1}{U} replaces the printed {2}{W}")
    void serveIsPaidWithItsOwnCost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ProtectServe()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, SERVE, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(-4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Fuse resolves Protect then Serve on independent targets")
    void fuseUsesIndependentTargets() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ProtectServe()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, FUSE, List.of(mine.getId(), theirs.getId()));
        harness.passBothPriorities();

        assertThat(mine.getEffectivePower()).isEqualTo(4);
        assertThat(mine.getEffectiveToughness()).isEqualTo(6);
        assertThat(theirs.getEffectivePower()).isEqualTo(-4);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fuse may put both halves on the same creature")
    void fuseAllowsSharedTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ProtectServe()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, FUSE, List.of(bears.getId(), bears.getId()));
        harness.passBothPriorities();

        // +2/+4 then -6/-0
        assertThat(bears.getEffectivePower()).isEqualTo(-2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Fuse cannot be cast for only one half's mana")
    void fuseRequiresBothHalvesCost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ProtectServe()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID bearsId = bears.getId();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, FUSE, List.of(bearsId, bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boosts wear off at end of turn")
    void wearsOff() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ProtectServe()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, FUSE, List.of(mine.getId(), theirs.getId()));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(mine.getEffectivePower()).isEqualTo(2);
        assertThat(mine.getEffectiveToughness()).isEqualTo(2);
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void fuseStillAffectsTheRemainingLegalTarget(boolean removeProtectTarget) {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new MazeRusher());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new MazeRusher());
        ProtectServe spell = new ProtectServe();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalInstant(player1, 0, FUSE, List.of(mine.getId(), theirs.getId()));

        Permanent removed = removeProtectTarget ? mine : theirs;
        var owner = removeProtectTarget ? player1 : player2;
        gd.playerBattlefields.get(owner.getId()).remove(removed);
        harness.setGraveyard(owner, List.of(removed.getCard()));
        harness.passBothPriorities();

        Permanent remaining = removeProtectTarget ? theirs : mine;
        assertThat(remaining.getEffectivePower()).isEqualTo(removeProtectTarget ? 0 : 8);
        assertThat(remaining.getEffectiveToughness()).isEqualTo(removeProtectTarget ? 3 : 7);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fuseDoesNotResolveWhenItsSharedTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MazeRusher());
        ProtectServe spell = new ProtectServe();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalInstant(player1, 0, FUSE, List.of(target.getId(), target.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingFromExileOffersOnlyIndividualHalves() {
        harness.addToBattlefield(player1, new PossibilityStorm());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MazeRusher());
        harness.setLibrary(player1, List.of(new ProtectServe()));
        harness.setHand(player1, List.of(new ProtectServe()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, PROTECT, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).hasSize(2);
        assertThat(choice.options()).noneMatch(option -> option.startsWith("Fuse"));
    }
}
