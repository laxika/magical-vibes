package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KitsuneDragonsDaughter.class, GrizzlyBears.class})
class KitsuneDragonsDaughterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may exchange control of two creatures controlled by different players")
    void etbExchangesControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castKitsune();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownCreature);
    }

    @Test
    @DisplayName("Declining the ETB exchange leaves both creatures under their original control")
    void decliningEtbExchangeDoesNothing() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castKitsune();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    @DisplayName("Combat damage may exchange control of two creatures controlled by different players")
    void combatDamageExchangesControl() {
        Permanent kitsune = addCreatureReady(player1, new KitsuneDragonsDaughter());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        kitsune.setAttacking(true);
        kitsune.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownCreature);
    }

    @Test
    @DisplayName("Targets exclude Kitsune and the second target must have a different controller")
    void targetsExcludeSourceAndSameController() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castKitsune();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice firstChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(firstChoice.validPermanentIds()).containsExactlyInAnyOrder(
                ownCreature.getId(), otherOwnCreature.getId(), opponentCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        PendingInteraction.PermanentChoice secondChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(secondChoice.validPermanentIds()).containsExactly(opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature, otherOwnCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownCreature);
    }

    @Test
    @DisplayName("If one target leaves before resolution, neither creature changes controller")
    void missingTargetPreventsEntireExchange() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castKitsune();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, opponentCreature));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature).doesNotContain(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(ownCreature, opponentCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCreature.getCard());
    }

    @Test
    @DisplayName("The exchange still resolves after Kitsune leaves the battlefield")
    void sourceLeavingDoesNotPreventExchange() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castKitsune();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        Permanent kitsune = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof KitsuneDragonsDaughter)
                .findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, kitsune));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature).doesNotContain(kitsune);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownCreature);
    }
    private void castKitsune() {
        harness.castFromHand(player1, new KitsuneDragonsDaughter(), "{4}{U}{U}");
    }
}
