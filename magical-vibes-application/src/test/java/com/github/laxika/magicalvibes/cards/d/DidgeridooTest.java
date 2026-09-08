package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AnabaShaman;
import com.github.laxika.magicalvibes.cards.f.Forget;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Didgeridoo.class, AnabaShaman.class, Forget.class})
class DidgeridooTest extends BaseCardTest {

    @Test
    @DisplayName("Ability offers only Minotaur permanent cards in hand")
    void abilityOffersOnlyMinotaurs() {
        addDidgeridoo();
        harness.setHand(player1, List.of(new Forget(), new Didgeridoo(), new AnabaShaman()));
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
    @DisplayName("Requires three generic mana to activate")
    void requiresThreeGenericManaToActivate() {
        addDidgeridoo();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
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
        assertThat(findPermanent(player1, "Didgeridoo").isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can choose any eligible Minotaur permanent card")
    void choosesAmongMultipleEligibleMinotaurs() {
        addDidgeridoo();
        AnabaShaman firstShaman = new AnabaShaman();
        Forget nonMinotaur = new Forget();
        AnabaShaman secondShaman = new AnabaShaman();
        harness.setHand(player1, List.of(firstShaman, nonMinotaur, secondShaman));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0, 2);

        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(firstShaman.getId()))
                .anyMatch(card -> card.getId().equals(nonMinotaur.getId()))
                .noneMatch(card -> card.getId().equals(secondShaman.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(secondShaman.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(firstShaman.getId()));
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

    @Test
    @DisplayName("Accepting with no eligible card leaves the hand unchanged")
    void noEligibleCardLeavesHandUnchanged() {
        addDidgeridoo();
        harness.setHand(player1, List.of(new Forget(), new Didgeridoo()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Forget");
        harness.assertInHand(player1, "Didgeridoo");
    }

    private void addDidgeridoo() {
        harness.addToBattlefield(player1, new Didgeridoo());
    }

    private void giveManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
