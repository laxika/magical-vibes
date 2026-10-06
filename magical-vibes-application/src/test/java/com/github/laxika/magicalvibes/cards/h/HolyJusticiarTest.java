package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AlchemistsApprentice;
import com.github.laxika.magicalvibes.cards.g.Ghoulflesh;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.n.NaturalEnd;
import com.github.laxika.magicalvibes.cards.u.UndeadExecutioner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HolyJusticiar.class, MoorlandInquisitor.class, UndeadExecutioner.class,
        AlchemistsApprentice.class, Ghoulflesh.class, NaturalEnd.class})
class HolyJusticiarTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a non-Zombie target and leaves it on the battlefield")
    void tapsNonZombie() {
        addJusticiar();
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        UUID targetId = harness.getPermanentId(player2, "Moorland Inquisitor");
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Moorland Inquisitor").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps and exiles a Zombie target")
    void exilesZombie() {
        addJusticiar();
        harness.addToBattlefield(player2, new UndeadExecutioner());
        UUID corpseId = harness.getPermanentId(player2, "Undead Executioner");
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, corpseId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Undead Executioner");
        harness.assertNotInGraveyard(player2, "Undead Executioner");
    }

    @Test
    void exilesAlreadyTappedZombieWithoutTriggeringItsDeathAbility() {
        addJusticiar();
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new UndeadExecutioner());
        zombie.tap();
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, zombie.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Undead Executioner");
        harness.assertNotInGraveyard(player2, "Undead Executioner");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(zombie.getCard().getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canExileControllersOwnZombie() {
        addJusticiar();
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new UndeadExecutioner());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, zombie.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Undead Executioner");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(zombie.getCard().getId()));
    }

    @Test
    void exilesCreatureMadeZombieByAura() {
        addJusticiar();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new Ghoulflesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Moorland Inquisitor");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(creature.getCard().getId()));
        harness.assertInGraveyard(player1, "Ghoulflesh");
    }

    @Test
    void doesNotExileCreatureThatStopsBeingZombieBeforeResolution() {
        addJusticiar();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new Ghoulflesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        UUID auraId = harness.getPermanentId(player1, "Ghoulflesh");
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.setHand(player2, List.of(new NaturalEnd()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castInstant(player2, 0, auraId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Moorland Inquisitor");
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void doesNothingWhenTargetIsSacrificedInResponse() {
        addJusticiar();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlchemistsApprentice());
        harness.setLibrary(player2, List.of(new MoorlandInquisitor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player2, 0, null, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alchemist's Apprentice");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationPaysManaAndTapsSourceBeforeResolution() {
        addJusticiar();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HolyJusticiar());
        source.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        addJusticiar();
        findPermanent(player1, "Holy Justiciar").tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutWhiteMana() {
        addJusticiar();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Holy Justiciar").isTapped()).isFalse();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        addJusticiar();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Ghoulflesh());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void addJusticiar() {
        Permanent justiciar = harness.addToBattlefieldAndReturn(player1, new HolyJusticiar());
        justiciar.setSummoningSick(false);
    }
}
