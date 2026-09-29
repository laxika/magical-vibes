package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import java.util.List;

@CardUsed({HorrorOfHorrors.class, DrudgeSkeletons.class, GlorySeeker.class, Swamp.class, Shock.class, Forest.class})
class HorrorOfHorrorsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Swamp puts the regeneration ability on the stack targeting the black creature")
    void activatingTargetsBlackCreature() {
        harness.addToBattlefield(player1, new HorrorOfHorrors());
        Permanent zombie = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, null, zombie.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(zombie.getId());
        // Swamp is sacrificed as a cost.
        harness.assertInGraveyard(player1, "Swamp");
    }

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield to the target black creature")
    void resolvingGrantsShield() {
        harness.addToBattlefield(player1, new HorrorOfHorrors());
        Permanent zombie = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, null, zombie.getId());
        harness.passBothPriorities();

        assertThat(zombie.getRegenerationShield()).isEqualTo(1);
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
        Permanent zombie = addCreatureReady(player1, new DrudgeSkeletons());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, zombie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A regeneration shield prevents lethal damage to the black creature")
    void regenerationShieldPreventsLethalDamage() {
        harness.addToBattlefield(player1, new HorrorOfHorrors());
        Permanent zombie = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, null, zombie.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, zombie.getId());

        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        harness.assertNotInGraveyard(player1, "Drudge Skeletons");
        assertThat(zombie.isTapped()).isTrue();
        assertThat(zombie.getRegenerationShield()).isZero();
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
