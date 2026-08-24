package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FarrelitePriest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Necrite.class, FarrelitePriest.class})
class NecriteTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new Necrite());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addDefenderCreature() {
        return addCreatureReady(player2, new FarrelitePriest());
    }

    private void advanceToMayChoice() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting the may sacrifices Necrite and destroys the chosen creature")
    void acceptSacrificeAndDestroy() {
        Permanent victim = addDefenderCreature();
        addAttacker();

        advanceToMayChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(victim.getId()));

        harness.assertNotOnBattlefield(player1, "Necrite");
        harness.assertInGraveyard(player1, "Necrite");

        harness.assertNotOnBattlefield(player2, "Farrelite Priest");
        harness.assertInGraveyard(player2, "Farrelite Priest");
    }

    @Test
    @DisplayName("The destroyed creature can't be regenerated")
    void cannotBeRegenerated() {
        Permanent victim = addDefenderCreature();
        victim.setRegenerationShield(1);
        addAttacker();

        advanceToMayChoice();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(victim.getId()));

        harness.assertNotOnBattlefield(player2, "Farrelite Priest");
        harness.assertInGraveyard(player2, "Farrelite Priest");
    }

    @Test
    @DisplayName("Declining the may keeps Necrite and the target")
    void declineKeepsBoth() {
        addDefenderCreature();
        addAttacker();

        advanceToMayChoice();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Necrite");
        harness.assertOnBattlefield(player2, "Farrelite Priest");
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

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        addDefenderCreature();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Necrite");
        harness.assertOnBattlefield(player2, "Farrelite Priest");
    }

    @Test
    @DisplayName("Only defending player's creatures can be targeted")
    void onlyDefendingPlayersCreaturesCanBeTargeted() {
        Permanent ownCreature = addCreatureReady(player1, new FarrelitePriest());
        Permanent victim = addDefenderCreature();
        addAttacker();

        advanceToMayChoice();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(victim.getId()).doesNotContain(ownCreature.getId());
    }

    @Test
    @DisplayName("Accepting the sacrifice does not allow declining the required creature target")
    void acceptingSacrificeRequiresCreatureTarget() {
        addDefenderCreature();
        addAttacker();

        advanceToMayChoice();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
