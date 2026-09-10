package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DauthiMindripper.class, SoltariFootSoldier.class})
class DauthiMindripperTest extends BaseCardTest {

    private Permanent addAttacker() {
        return addCreatureReady(player1, new DauthiMindripper());
    }

    private int attackerIndex(Permanent attacker) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
    }

    private void declareUnblockedAttack(Permanent attacker) {
        declareAttackers(List.of(attackerIndex(attacker)));
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Accepting the may sacrifices it and the defending player discards three cards")
    void unblockedAcceptSacrificeAndDiscardThree() {
        harness.setHand(player2, List.of(
                new SoltariFootSoldier(), new SoltariFootSoldier(), new SoltariFootSoldier()));
        declareUnblockedAttack(addAttacker());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Dauthi Mindripper");
        harness.assertInGraveyard(player1, "Dauthi Mindripper");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(3);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Declining the may keeps it on the battlefield and forces no discard")
    void unblockedDeclineKeepsCreature() {
        harness.setHand(player2, List.of(
                new SoltariFootSoldier(), new SoltariFootSoldier(), new SoltariFootSoldier()));
        declareUnblockedAttack(addAttacker());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Dauthi Mindripper");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The contingent discard does not happen if the Mindripper leaves before resolution")
    void unblockedNoDiscardWhenCreatureLeavesBeforeResolution() {
        harness.setHand(player2, List.of(
                new SoltariFootSoldier(), new SoltariFootSoldier(), new SoltariFootSoldier()));
        Permanent attacker = addAttacker();
        declareUnblockedAttack(attacker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        gd.playerBattlefields.get(player1.getId()).remove(attacker);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Blocked by a shadow creature does not trigger the ability")
    void blockedNoTrigger() {
        harness.setHand(player2, List.of(
                new SoltariFootSoldier(), new SoltariFootSoldier(), new SoltariFootSoldier()));

        Permanent blocker = addCreatureReady(player2, new SoltariFootSoldier());
        Permanent attacker = addAttacker();

        declareAttackers(List.of(attackerIndex(attacker)));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), attackerIndex(attacker))));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Dauthi Mindripper");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An attacker triggers when the defender could block but chooses not to")
    void unblockedWhenDefenderDeclinesToBlock() {
        harness.setHand(player2, List.of(
                new SoltariFootSoldier(), new SoltariFootSoldier(), new SoltariFootSoldier()));
        addCreatureReady(player2, new SoltariFootSoldier());
        Permanent attacker = addAttacker();

        declareAttackers(List.of(attackerIndex(attacker)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Dauthi Mindripper");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Accepting the may discards only the cards available in a smaller hand")
    void unblockedAcceptSacrificeAndDiscardAvailableCards() {
        harness.setHand(player2, List.of(new SoltariFootSoldier(), new SoltariFootSoldier()));
        declareUnblockedAttack(addAttacker());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(3);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Accepting the may with an empty hand still sacrifices the Mindripper")
    void unblockedAcceptSacrificeWithEmptyHand() {
        harness.setHand(player2, List.of());
        declareUnblockedAttack(addAttacker());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Dauthi Mindripper");
        harness.assertInGraveyard(player1, "Dauthi Mindripper");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
