package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.r.RishadanAirship;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CausticWasps.class, CreditVoucher.class, DeadlyInsect.class, RishadanAirship.class})
class CausticWaspsTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the combat damage trigger destroys an artifact the damaged player controls")
    void destroysDamagedPlayersArtifact() {
        Permanent wasps = addCreatureReady(player1, new CausticWasps());
        wasps.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CreditVoucher());

        resolveCombat();

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Credit Voucher");
        harness.assertInGraveyard(player2, "Credit Voucher");
    }

    @Test
    @DisplayName("Declining the combat damage trigger leaves the artifact on the battlefield")
    void declineLeavesArtifact() {
        Permanent wasps = addCreatureReady(player1, new CausticWasps());
        wasps.setAttacking(true);
        harness.addToBattlefield(player2, new CreditVoucher());

        resolveCombat();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Credit Voucher"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Credit Voucher");
    }

    @Test
    @DisplayName("Only artifacts controlled by the damaged player are legal targets")
    void onlyDamagedPlayersArtifactsAreLegalTargets() {
        Permanent wasps = addCreatureReady(player1, new CausticWasps());
        wasps.setAttacking(true);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new CreditVoucher());
        Permanent enemyCreature = addCreatureReady(player2, new DeadlyInsect());
        Permanent enemyArtifact = harness.addToBattlefieldAndReturn(player2, new CreditVoucher());

        resolveCombat();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(enemyArtifact.getId())
                .doesNotContain(ownArtifact.getId(), enemyCreature.getId());
    }

    @Test
    @DisplayName("No combat damage trigger is offered when the damaged player controls no artifacts")
    void noTriggerWithoutArtifacts() {
        Permanent wasps = addCreatureReady(player1, new CausticWasps());
        wasps.setAttacking(true);
        addCreatureReady(player2, new DeadlyInsect());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Combat damage dealt to a creature does not trigger the ability")
    void noTriggerWhenBlocked() {
        Permanent wasps = addCreatureReady(player1, new CausticWasps());
        wasps.setAttacking(true);
        addCreatureReady(player2, new RishadanAirship());
        harness.addToBattlefield(player2, new CreditVoucher());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Credit Voucher");
    }
}
