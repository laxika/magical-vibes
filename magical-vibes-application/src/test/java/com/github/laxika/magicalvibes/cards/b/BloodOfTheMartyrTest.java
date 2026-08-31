package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FireDrake;
import com.github.laxika.magicalvibes.cards.i.Inferno;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodOfTheMartyr.class, BrothersOfFire.class, FireDrake.class, Inferno.class})
class BloodOfTheMartyrTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects damage to any creature to the spell's controller")
    void redirectsDamageToAnyCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent target = addCreatureReady(player2, new FireDrake());
        Permanent brothers = addCreatureReady(player2, new BrothersOfFire());

        castBloodOfTheMartyr();

        activateBrothersOfFire(brothers, target.getId());
        acceptAllRedirectChoices();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The redirect expires at the end of the turn")
    void redirectExpiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent target = addCreatureReady(player2, new FireDrake());
        Permanent brothers = addCreatureReady(player2, new BrothersOfFire());

        castBloodOfTheMartyr();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        activateBrothersOfFire(brothers, target.getId());
        acceptAllRedirectChoices();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Redirects combat damage that would be dealt to a creature")
    void redirectsCombatDamageToController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent blocker = addCreatureReady(player1, new FireDrake());
        Permanent attacker = addCreatureReady(player2, new BrothersOfFire());

        castBloodOfTheMartyr();

        declareAttackers(player2, List.of(indexOf(player2, attacker)));

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(indexOf(player1, blocker), indexOf(player2, attacker))));
        resolveCombat(player2);
        acceptAllRedirectChoices();

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    void redirectsDamageToOwnCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent target = addCreatureReady(player1, new FireDrake());
        Permanent brothers = addCreatureReady(player2, new BrothersOfFire());

        castBloodOfTheMartyr();

        activateBrothersOfFire(brothers, target.getId());
        acceptAllRedirectChoices();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void doesNotRedirectDamageToPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent brothers = addCreatureReady(player2, new BrothersOfFire());

        castBloodOfTheMartyr();

        activateBrothersOfFire(brothers, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void redirectsSpellDamageToCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent target = addCreatureReady(player2, new FireDrake());

        castBloodOfTheMartyr();

        harness.setHand(player2, List.of(new Inferno()));
        harness.addMana(player2, ManaColor.RED, 7);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        acceptAllRedirectChoices();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(8);
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void canDeclineRedirectingDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent target = addCreatureReady(player2, new FireDrake());
        Permanent brothers = addCreatureReady(player2, new BrothersOfFire());

        castBloodOfTheMartyr();

        activateBrothersOfFire(brothers, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private void castBloodOfTheMartyr() {
        harness.setHand(player1, List.of(new BloodOfTheMartyr()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void activateBrothersOfFire(Permanent brothers, UUID targetId) {
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.activateAbility(player2, indexOf(player2, brothers), null, targetId);
        harness.passBothPriorities();
    }

    private void acceptAllRedirectChoices() {
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
