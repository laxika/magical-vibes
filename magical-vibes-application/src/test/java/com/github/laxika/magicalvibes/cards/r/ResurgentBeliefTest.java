package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CapashenKnight;
import com.github.laxika.magicalvibes.cards.p.PatternOfRebirth;
import com.github.laxika.magicalvibes.cards.s.Sanctimony;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResurgentBelief.class, Sanctimony.class, PatternOfRebirth.class, CapashenKnight.class})
class ResurgentBeliefTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Resurgent Belief with two time counters")
    void suspendExilesWithTwoTimeCounters() {
        ResurgentBelief card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast that returns enchantments")
    void lastCounterOffersFreeCastThatReturnsEnchantments() {
        ResurgentBelief card = suspendCard();
        Card enchantment = new Sanctimony();
        Card creature = new CapashenKnight();
        harness.setGraveyard(player1, List.of(enchantment, creature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sanctimony");
        harness.assertNotOnBattlefield(player1, "Capashen Knight");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Leaves an Aura with nothing to enchant in the graveyard")
    void leavesOrphanedAuraInGraveyard() {
        Card aura = new PatternOfRebirth();
        ResurgentBelief card = suspendCard();
        harness.setGraveyard(player1, List.of(aura));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pattern of Rebirth");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    private ResurgentBelief suspendCard() {
        ResurgentBelief card = new ResurgentBelief();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
