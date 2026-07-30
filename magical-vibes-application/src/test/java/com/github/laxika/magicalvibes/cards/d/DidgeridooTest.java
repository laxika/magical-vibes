package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AnabaShaman;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DidgeridooTest extends BaseCardTest {

    @Test
    @DisplayName("Ability offers only Minotaur permanent cards in hand")
    void abilityOffersOnlyMinotaurs() {
        addDidgeridoo();
        harness.setHand(player1, List.of(new Mountain(), new GrizzlyBears(), new AnabaShaman()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(2);
    }

    @Test
    @DisplayName("Chosen Minotaur enters the battlefield untapped from hand")
    void chosenMinotaurEntersBattlefield() {
        addDidgeridoo();
        harness.setHand(player1, List.of(new AnabaShaman()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent shaman = findPermanent(player1, "Anaba Shaman");
        assertThat(shaman.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining leaves the Minotaur in hand")
    void decliningLeavesMinotaurInHand() {
        addDidgeridoo();
        harness.setHand(player1, List.of(new AnabaShaman()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Anaba Shaman");
    }

    private void addDidgeridoo() {
        harness.addToBattlefield(player1, new Didgeridoo());
    }

    private void giveManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
