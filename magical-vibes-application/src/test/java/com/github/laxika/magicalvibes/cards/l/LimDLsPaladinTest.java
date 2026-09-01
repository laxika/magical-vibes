package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LimDLsPaladin.class)
class LimDLsPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking unblocked drains the defending player for 4 and prevents its combat damage")
    void unblockedDrainsFourAndAssignsNoDamage() {
        Permanent paladin = addAttackingPaladin(player1, player2);
        paladin.setPowerModifier(1);
        int startingLife = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 4);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(paladin.getId());
    }

    @Test
    @DisplayName("Becoming blocked grants +6/+3, tramples, and skips the unblocked drain")
    void blockedGetsPlusSixPlusThree() {
        Permanent paladin = addAttackingPaladin(player1, player2);
        Permanent blocker = addCreatureReady(player2, new LimDLsPaladin());
        int startingLife = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CombatDamageAssigned(0, Map.of(
                        blocker.getId(), 3,
                        player2.getId(), 3)));

        assertThat(paladin.getPowerModifier()).isEqualTo(6);
        assertThat(paladin.getToughnessModifier()).isEqualTo(3);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(paladin.getId());
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 3);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures grants +6/+3 only once")
    void blockedByMultipleCreaturesGetsOnlyOneBoost() {
        Permanent paladin = addAttackingPaladin(player1, player2);
        Permanent firstBlocker = addCreatureReady(player2, new LimDLsPaladin());
        Permanent secondBlocker = addCreatureReady(player2, new LimDLsPaladin());
        int startingLife = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CombatDamageAssigned(0, Map.of(
                        firstBlocker.getId(), 3,
                        secondBlocker.getId(), 3)));

        assertThat(paladin.getPowerModifier()).isEqualTo(6);
        assertThat(paladin.getToughnessModifier()).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(firstBlocker, secondBlocker);
    }

    @Test
    @DisplayName("Upkeep: discarding keeps the Paladin and draws nothing")
    void upkeepDiscardKeepsPaladin() {
        addCreatureReady(player1, new LimDLsPaladin());
        harness.setHand(player1, List.of(new LimDLsPaladin()));
        int deckSize = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Lim-Dûl's Paladin");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSize);
    }

    @Test
    @DisplayName("Upkeep: declining the discard sacrifices the Paladin and draws a card")
    void upkeepDeclineSacrificesAndDraws() {
        addCreatureReady(player1, new LimDLsPaladin());
        harness.setHand(player1, List.of(new LimDLsPaladin()));
        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();
        int deckSize = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Lim-Dûl's Paladin")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lim-Dûl's Paladin"));
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSize - 1);
    }

    @Test
    @DisplayName("Upkeep with an empty hand sacrifices the Paladin and draws without prompting")
    void upkeepEmptyHandSacrificesAndDraws() {
        addCreatureReady(player1, new LimDLsPaladin());
        harness.setHand(player1, List.of());
        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // No "you may discard" prompt is raised — with an empty hand the penalty applies straight away.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Lim-Dûl's Paladin")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
    }

    private Permanent addAttackingPaladin(Player attacker, Player defender) {
        Permanent perm = addCreatureReady(attacker, new LimDLsPaladin());
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}
