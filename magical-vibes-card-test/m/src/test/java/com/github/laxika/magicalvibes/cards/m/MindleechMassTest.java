package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindleechMass.class, BorosRecruit.class, ElvesOfDeepShadow.class, Forest.class})
class MindleechMassTest extends BaseCardTest {

    @Test
    @CardUsed(BorosRecruit.class)
    @DisplayName("Combat damage offers a spell from the damaged player's hand for free")
    void castsSpellFromDamagedPlayersHandForFree() {
        addAttackingMindleechMass(player1);
        BorosRecruit controllerCard = new BorosRecruit();
        BorosRecruit damagedPlayerCard = new BorosRecruit();
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
        harness.assertOnBattlefield(player1, "Boros Recruit");
    }

    @Test
    @CardUsed(BorosRecruit.class)
    @DisplayName("Declining leaves the damaged player's spell in hand")
    void decliningLeavesSpellInDamagedPlayersHand() {
        addAttackingMindleechMass(player1);
        BorosRecruit damagedPlayerCard = new BorosRecruit();
        harness.setHand(player2, new ArrayList<>(List.of(damagedPlayerCard)));

        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(damagedPlayerCard.getId()));
    }

    @Test
    @CardUsed(Forest.class)
    @DisplayName("A land-only hand can be inspected but no land can be cast")
    void doesNotOfferLand() {
        addAttackingMindleechMass(player1);
        harness.setHand(player2, List.of(new Forest()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({MindleechMass.class, BorosRecruit.class})
    @DisplayName("Blocked trample damage that does not reach a player does not trigger")
    void doesNotTriggerWithoutCombatDamageToPlayer() {
        addAttackingMindleechMass(player1);
        Permanent blocker = addCreatureReady(player2, new MindleechMass());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, List.of(new BorosRecruit()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({BorosRecruit.class, ElvesOfDeepShadow.class})
    @DisplayName("Trample damage that reaches a player offers one of multiple hand spells")
    void offersOnlyOneSpellAfterPartialTrampleDamage() {
        addAttackingMindleechMass(player1);
        Permanent blocker = addCreatureReady(player2, new ElvesOfDeepShadow());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        BorosRecruit firstCard = new BorosRecruit();
        BorosRecruit secondCard = new BorosRecruit();
        harness.setHand(player2, new ArrayList<>(List.of(firstCard, secondCard)));

        resolveCombat();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class)).isNotNull();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 5
        ));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(firstCard.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(secondCard);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(firstCard.getId()));
    }

    @Test
    @CardUsed({MindleechMass.class, BorosRecruit.class})
    @DisplayName("Declining to cast does not publicly identify a card in the damaged player's hand")
    void decliningDoesNotRevealPrivateHandCardInPublicLog() {
        addAttackingMindleechMass(player1);
        harness.setHand(player2, List.of(new BorosRecruit()));

        resolveCombatAndTrigger();

        if (gd.pendingMayAbilities.getFirst().sourceCard() instanceof MindleechMass) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gameLogContains("Boros Recruit")).isFalse();
        harness.assertInHand(player2, "Boros Recruit");
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
