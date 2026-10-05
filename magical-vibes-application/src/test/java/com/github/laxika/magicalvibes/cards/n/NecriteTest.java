package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DeathSpeakers;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Necrite.class, ScatheZombies.class, DeathSpeakers.class, RayOfCommand.class})
class NecriteTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new Necrite());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addDefenderCreature() {
        return addCreatureReady(player2, new ScatheZombies());
    }

    private void declareNoBlocks() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
    }

    private void advanceToMayChoice(Permanent target) {
        declareNoBlocks();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting the may sacrifices Necrite and destroys the chosen creature")
    void acceptSacrificeAndDestroy() {
        Permanent victim = addDefenderCreature();
        addAttacker();

        advanceToMayChoice(victim);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Necrite");
        harness.assertInGraveyard(player1, "Necrite");

        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        harness.assertInGraveyard(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("The destroyed creature can't be regenerated")
    void cannotBeRegenerated() {
        Permanent victim = addDefenderCreature();
        victim.setRegenerationShield(1);
        addAttacker();

        advanceToMayChoice(victim);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        harness.assertInGraveyard(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("Declining the may keeps Necrite and the target")
    void declineKeepsBoth() {
        Permanent victim = addDefenderCreature();
        addAttacker();

        advanceToMayChoice(victim);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Necrite");
        harness.assertOnBattlefield(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("Blocked attacker does not trigger the ability")
    void blockedNoTrigger() {
        Permanent blocker = addDefenderCreature();

        addAttacker();

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, 0)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Necrite");
    }

    @Test
    @DisplayName("A creature appearing after the trigger is put on the stack is not a target")
    void targetMustExistWhenTriggerIsPutOnStack() {
        addAttacker();

        declareNoBlocks();
        addDefenderCreature();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Necrite");
        harness.assertOnBattlefield(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("Only defending player's creatures can be targeted")
    void onlyDefendingPlayersCreaturesCanBeTargeted() {
        Permanent ownCreature = addCreatureReady(player1, new ScatheZombies());
        Permanent victim = addDefenderCreature();
        addAttacker();

        declareNoBlocks();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(victim.getId())
                .doesNotContain(ownCreature.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("The creature target is chosen before the sacrifice decision")
    void targetIsChosenBeforeSacrificeDecision() {
        Permanent victim = addDefenderCreature();
        addAttacker();

        declareNoBlocks();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.assertOnBattlefield(player1, "Necrite");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("A creature protected from black is not a legal target")
    void protectedCreatureIsNotATarget() {
        addCreatureReady(player2, new DeathSpeakers());
        addAttacker();

        declareNoBlocks();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A stolen Necrite cannot be sacrificed by its former controller")
    void stolenSourceCannotBeSacrificed() {
        Permanent victim = addDefenderCreature();
        Permanent attacker = addAttacker();

        declareNoBlocks();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Necrite");
        harness.assertOnBattlefield(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("A chosen creature that changes controller makes the ability fail without sacrifice")
    void targetChangingControllerPreventsSacrifice() {
        Permanent victim = addDefenderCreature();
        addAttacker();

        declareNoBlocks();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Scathe Zombies");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Necrite");
        harness.assertOnBattlefield(player1, "Scathe Zombies");
    }

    @Test
    @DisplayName("The sacrificed attacker deals no combat damage")
    void sacrificeOccursBeforeCombatDamage() {
        Permanent victim = addDefenderCreature();
        addAttacker();

        advanceToMayChoice(victim);
        harness.handleMayAbilityChosen(player1, true);
        resolveCombat();

        harness.assertInGraveyard(player1, "Necrite");
        harness.assertLife(player2, 20);
    }
}
