package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.w.WanderingWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ghostform.class, WanderingWolf.class, Forest.class, Cloudshift.class})
class GhostformTest extends BaseCardTest {

    @Test
    @DisplayName("Both target creatures can't be blocked this turn")
    void makesBothTargetsUnblockable() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WanderingWolf());
        harness.setHand(player1, List.of(new Ghostform()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.isCantBeBlocked()).isTrue();
        assertThat(second.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Can be cast with a single target")
    void worksWithOneTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        harness.setHand(player1, List.of(new Ghostform()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(bears.getId()));

        assertThat(bears.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        harness.setHand(player1, List.of(new Ghostform()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(bears.getId()));

        assertThat(bears.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Ghostform()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(forestId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Can resolve without choosing any targets")
    void worksWithZeroTargets() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        harness.setHand(player1, List.of(new Ghostform()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(wolf.isCantBeBlocked()).isFalse();
        harness.assertInGraveyard(player1, "Ghostform");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose more than two creatures")
    void rejectsThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new WanderingWolf());
        harness.setHand(player1, List.of(new Ghostform()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void rejectsDuplicateTargets() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        harness.setHand(player1, List.of(new Ghostform()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(wolf.getId(), wolf.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolves for the remaining target when another target is blinked")
    void resolvesForRemainingLegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WanderingWolf());
        harness.setHand(player1, List.of(new Ghostform(), new Cloudshift()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.getId()).isNotEqualTo(first.getId());
        assertThat(returned.isCantBeBlocked()).isFalse();
        assertThat(second.isCantBeBlocked()).isTrue();
        harness.assertInGraveyard(player1, "Ghostform");
    }

    @Test
    @DisplayName("Does not affect a blinked creature when its only target becomes illegal")
    void doesNotAffectReturnedCreatureWhenAllTargetsIllegal() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        harness.setHand(player1, List.of(new Ghostform(), new Cloudshift()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, List.of(wolf.getId()));
        harness.castAndResolveInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.getId()).isNotEqualTo(wolf.getId());
        assertThat(returned.isCantBeBlocked()).isFalse();
        harness.assertInGraveyard(player1, "Ghostform");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A targeted attacker cannot be blocked by a creature of equal power")
    void preventsBlockerDeclaration() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        harness.addToBattlefield(player2, new WanderingWolf());
        harness.setHand(player1, List.of(new Ghostform()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(attacker.getId()));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
