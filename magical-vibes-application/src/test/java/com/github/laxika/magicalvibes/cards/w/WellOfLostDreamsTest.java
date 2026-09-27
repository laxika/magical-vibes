package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.v.VedalkenEngineer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WellOfLostDreams.class, VedalkenEngineer.class})
class WellOfLostDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Caps X at the life gained and draws the amount paid")
    void capsXAtLifeGainedAndDrawsPaidAmount() {
        harness.addToBattlefield(player1, new WellOfLostDreams());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Choosing zero declines the payment")
    void choosingZeroDeclinesPayment() {
        harness.addToBattlefield(player1, new WellOfLostDreams());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not prompt when no mana is available")
    void noManaDoesNotPrompt() {
        harness.addToBattlefield(player1, new WellOfLostDreams());
        harness.setLife(player1, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Cannot pay a triggered ability with mana restricted to artifact spells and activations")
    void cannotPayWithArtifactRestrictedMana() {
        harness.addToBattlefield(player1, new WellOfLostDreams());
        addCreatureReady(player1, new VedalkenEngineer());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isEqualTo(2);
    }
}
