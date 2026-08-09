package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NightshadeSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature -X/-X for the number of selected black cards")
    void givesTargetCreatureMinusForSelectedBlackCards() {
        Permanent seer = addReadySeer();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DarkRitual blackCard = new DarkRitual();
        harness.setHand(player1, List.of(blackCard, new Shock()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(blackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Allows revealing zero black cards")
    void allowsRevealingZeroBlackCards() {
        addReadySeer();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadySeer();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2,
                new com.github.laxika.magicalvibes.cards.f.FountainOfYouth());
        harness.setHand(player1, List.of(new DarkRitual()));
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySeer() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new NightshadeSeer());
        seer.setSummoningSick(false);
        return seer;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
