package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.ForceOfNature;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindleechMass.class, Forest.class, ForceOfNature.class, GrizzlyBears.class})
class MindleechMassTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage offers a spell from the damaged player's hand for free")
    void castsSpellFromDamagedPlayersHandForFree() {
        addAttackingMindleechMass(player1);
        GrizzlyBears controllerCard = new GrizzlyBears();
        GrizzlyBears damagedPlayerCard = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(controllerCard)));
        harness.setHand(player2, new ArrayList<>(List.of(damagedPlayerCard)));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities.getFirst().sourceCard().getId()).isEqualTo(damagedPlayerCard.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(damagedPlayerCard.getId());
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(controllerCard.getId()));

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the damaged player's spell in hand")
    void decliningLeavesSpellInDamagedPlayersHand() {
        addAttackingMindleechMass(player1);
        GrizzlyBears damagedPlayerCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(damagedPlayerCard)));

        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(damagedPlayerCard.getId()));
    }

    @Test
    @DisplayName("Lands in the damaged player's hand are not offered")
    void doesNotOfferLand() {
        addAttackingMindleechMass(player1);
        harness.setHand(player2, List.of(new Forest()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Blocked trample damage that does not reach a player does not trigger")
    void doesNotTriggerWithoutCombatDamageToPlayer() {
        addAttackingMindleechMass(player1);
        Permanent blocker = addCreatureReady(player2, new ForceOfNature());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAttackingMindleechMass(Player player) {
        Permanent mindleechMass = addCreatureReady(player, new MindleechMass());
        mindleechMass.setAttacking(true);
        return mindleechMass;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
