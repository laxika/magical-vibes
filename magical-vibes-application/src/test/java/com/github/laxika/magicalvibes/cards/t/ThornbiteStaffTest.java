package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BoskBanneret;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThornbiteStaffTest extends BaseCardTest {

    // ===== Granted ability: {2}, {T}: deal 1 damage to any target =====

    @Test
    @DisplayName("Equipped creature can pay {2} and tap to deal 1 damage to a player")
    void grantedAbilityDeals1DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature loses the granted ability when the Staff is removed")
    void creatureLosesAbilityWhenStaffRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(staff);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    // ===== Granted trigger: whenever a creature dies, untap this creature =====

    @Test
    @DisplayName("Equipped creature untaps whenever a creature dies")
    void untapsEquippedCreatureWhenCreatureDies() {
        Permanent creature = addReadyCreature(player1);
        creature.tap();
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());

        harness.addToBattlefield(player2, new GrizzlyBears());

        castCruelEdictAtPlayer2();
        harness.passBothPriorities(); // resolve Cruel Edict → creature dies → untap trigger on stack
        harness.passBothPriorities(); // resolve untap trigger

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap trigger fizzles while the Staff is unattached")
    void untapTriggerFizzlesWhenUnattached() {
        Permanent creature = addReadyCreature(player1);
        creature.tap();
        addStaffReady(player1); // left unattached

        harness.addToBattlefield(player2, new GrizzlyBears());

        castCruelEdictAtPlayer2();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Trigger: Shaman creature enters =====

    @Test
    @DisplayName("Accepting the may attaches the Staff to the Shaman that entered")
    void attachesToEnteringShamanOnAccept() {
        Permanent staff = addStaffReady(player1);

        harness.setHand(player1, List.of(new BoskBanneret()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature → Staff triggers, may-ability on stack
        harness.passBothPriorities(); // resolve may-ability → prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent shaman = shamanOnBattlefield(player1);
        assertThat(staff.getAttachedTo()).isEqualTo(shaman.getId());
    }

    @Test
    @DisplayName("Does not trigger for a non-Shaman creature entering")
    void doesNotTriggerForNonShaman() {
        addStaffReady(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature — no trigger for a Bear

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private void castCruelEdictAtPlayer2() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
    }

    private Permanent addStaffReady(Player player) {
        Permanent perm = new Permanent(new ThornbiteStaff());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent shamanOnBattlefield(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bosk Banneret"))
                .findFirst().orElseThrow();
    }
}
