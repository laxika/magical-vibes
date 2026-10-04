package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.u.UndeadMinotaur;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GnawingZombie.class, UndeadMinotaur.class})
class GnawingZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature drains 1 life from the target player")
    void drainsTargetPlayer() {
        addCreatureReady(player1, new GnawingZombie());
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new UndeadMinotaur());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, minotaur.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
        harness.assertInGraveyard(player1, "Undead Minotaur");
    }

    @Test
    @DisplayName("Can sacrifice itself to its own ability")
    void canSacrificeItself() {
        addCreatureReady(player1, new GnawingZombie());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
        harness.assertNotOnBattlefield(player1, "Gnawing Zombie");
        harness.assertInGraveyard(player1, "Gnawing Zombie");
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        addCreatureReady(player1, new GnawingZombie());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new GnawingZombie());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not require tapping, so it can be activated the turn it enters")
    void doesNotRequireTap() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new GnawingZombie());
        perm.setSummoningSick(true);
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new UndeadMinotaur());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, minotaur.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution and a tapped Zombie can activate repeatedly")
    void tappedZombieCanActivateRepeatedly() {
        Permanent zombie = addCreatureReady(player1, new GnawingZombie());
        zombie.setTapped(true);
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new UndeadMinotaur());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, minotaur.getId());

        harness.assertInGraveyard(player1, "Undead Minotaur");
        harness.assertNotOnBattlefield(player1, "Undead Minotaur");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Gnawing Zombie");
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Targeting yourself at 1 life finishes gaining life before checking player loss")
    void selfTargetAtOneLifeSurvivesResolution() {
        addCreatureReady(player1, new GnawingZombie());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertInGraveyard(player1, "Gnawing Zombie");
    }

    @Test
    @DisplayName("Life gain belongs to the activating controller on either side of the table")
    void playerTwoControlsLifeGain() {
        addCreatureReady(player2, new GnawingZombie());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
        harness.assertInGraveyard(player2, "Gnawing Zombie");
    }

    @Test
    @DisplayName("The ability cannot target a creature instead of a player")
    void cannotTargetCreature() {
        addCreatureReady(player1, new GnawingZombie());
        Permanent minotaur = harness.addToBattlefieldAndReturn(player2, new UndeadMinotaur());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, minotaur.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Gnawing Zombie");
        harness.assertOnBattlefield(player2, "Undead Minotaur");
    }
}
