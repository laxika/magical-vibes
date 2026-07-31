package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LimDLsPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking unblocked drains the defending player for 4 and prevents its combat damage")
    void unblockedDrainsFourAndAssignsNoDamage() {
        Permanent paladin = addAttackingPaladin(player1, player2);
        int startingLife = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 4);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(paladin.getId());
    }

    @Test
    @DisplayName("Becoming blocked grants +6/+3 and skips the unblocked drain")
    void blockedGetsPlusSixPlusThree() {
        Permanent paladin = addAttackingPaladin(player1, player2);
        addCreatureReady(player2, new GrizzlyBears());
        int startingLife = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(paladin.getPowerModifier()).isEqualTo(6);
        assertThat(paladin.getToughnessModifier()).isEqualTo(3);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(paladin.getId());
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Upkeep: discarding keeps the Paladin and draws nothing")
    void upkeepDiscardKeepsPaladin() {
        addCreatureReady(player1, new LimDLsPaladin());
        harness.setHand(player1, List.of(new Forest()));
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
        harness.setHand(player1, List.of(new Forest()));
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
        Permanent perm = new Permanent(new LimDLsPaladin());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(attacker.getId()).add(perm);
        return perm;
    }
}
