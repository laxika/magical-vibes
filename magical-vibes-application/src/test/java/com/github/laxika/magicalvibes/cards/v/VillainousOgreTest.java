package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GutwrencherOni;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VillainousOgre.class, GutwrencherOni.class, HumbleBudoka.class})
class VillainousOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Villainous Ogre cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new VillainousOgre());

        Permanent attacker = addCreatureReady(player1, new HumbleBudoka());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Regeneration ability cannot be activated without a Demon")
    void cannotRegenerateWithoutDemon() {
        addCreatureReady(player1, new VillainousOgre());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regeneration ability can be activated while you control a Demon")
    void canRegenerateWithDemon() {
        Permanent ogre = addCreatureReady(player1, new VillainousOgre());
        addCreatureReady(player1, new GutwrencherOni());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(ogre.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Demon controlled by an opponent does not enable the regeneration ability")
    void opponentsDemonDoesNotEnableRegeneration() {
        addCreatureReady(player1, new VillainousOgre());
        addCreatureReady(player2, new GutwrencherOni());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A regeneration shield saves Villainous Ogre from lethal combat damage")
    void regenerationShieldPreventsCombatDestruction() {
        Permanent ogre = addCreatureReady(player1, new VillainousOgre());
        addCreatureReady(player1, new GutwrencherOni());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        ogre.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HumbleBudoka());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ogre);
        assertThat(ogre.getRegenerationShield()).isZero();
        assertThat(ogre.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An activated regeneration ability resolves after the Demon leaves")
    void activatedAbilityResolvesAfterDemonLeaves() {
        Permanent ogre = addCreatureReady(player1, new VillainousOgre());
        Permanent demon = addCreatureReady(player1, new GutwrencherOni());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, demon));
        harness.passBothPriorities();

        assertThat(ogre.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Changeling you control enables the regeneration ability")
    @CardUsed(WoodlandChangeling.class)
    void changelingEnablesRegeneration() {
        Permanent ogre = addCreatureReady(player1, new VillainousOgre());
        addCreatureReady(player1, new WoodlandChangeling());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ogre.getRegenerationShield()).isEqualTo(1);
    }
}
