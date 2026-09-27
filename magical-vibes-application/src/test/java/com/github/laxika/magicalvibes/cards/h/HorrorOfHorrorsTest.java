package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HorrorOfHorrors.class, DrudgeSkeletons.class, GlorySeeker.class, Swamp.class, Forest.class})
class HorrorOfHorrorsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Swamp puts the regeneration ability on the stack targeting the black creature")
    void activatingTargetsBlackCreature() {
        harness.addToBattlefield(player1, new HorrorOfHorrors());
        Permanent skeleton = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, null, skeleton.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(skeleton.getId());
        // Swamp is sacrificed as a cost.
        harness.assertInGraveyard(player1, "Swamp");
    }

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield to the target black creature")
    void resolvingGrantsShield() {
        harness.addToBattlefield(player1, new HorrorOfHorrors());
        Permanent skeleton = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, null, skeleton.getId());
        harness.passBothPriorities();

        assertThat(skeleton.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-black creature")
    void cannotTargetNonBlackCreature() {
        harness.addToBattlefield(player1, new HorrorOfHorrors());
        Permanent whiteCreature = addCreatureReady(player1, new GlorySeeker());
        harness.addToBattlefield(player1, new Swamp());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, whiteCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
    }

    @Test
    @DisplayName("Cannot activate the ability without a Swamp to sacrifice")
    void cannotActivateWithoutSwamp() {
        harness.addToBattlefield(player1, new HorrorOfHorrors());
        Permanent skeleton = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, skeleton.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target an opponent's black creature")
    void canTargetOpponentsBlackCreature() {
        harness.addToBattlefield(player1, new HorrorOfHorrors());
        harness.addToBattlefield(player1, new Swamp());
        Permanent opponentSkeleton = addCreatureReady(player2, new DrudgeSkeletons());

        harness.activateAbility(player1, 0, null, opponentSkeleton.getId());
        harness.passBothPriorities();

        assertThat(opponentSkeleton.getRegenerationShield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Swamp");
    }

    @Test
    @DisplayName("Cannot target a black noncreature permanent")
    void cannotTargetBlackNoncreature() {
        harness.addToBattlefield(player1, new HorrorOfHorrors());
        Permanent blackEnchantment = harness.addToBattlefieldAndReturn(player1, new HorrorOfHorrors());
        harness.addToBattlefield(player1, new Swamp());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blackEnchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
        harness.assertNotInGraveyard(player1, "Swamp");
    }
}
