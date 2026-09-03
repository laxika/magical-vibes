package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GenePollinator.class, FountainOfYouth.class})
class GenePollinatorTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself and another permanent, then adds one mana of the chosen color")
    void tapsItselfAndAnotherPermanentForMana() {
        Permanent pollinator = addCreatureReady(player1, new GenePollinator());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        int pollinatorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pollinator);
        harness.activateAbility(player1, pollinatorIndex, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(pollinator.isTapped()).isTrue();
        assertThat(fountain.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Prompts which untapped permanent to tap when multiple are available")
    void promptsForPermanentChoice() {
        Permanent pollinator = addCreatureReady(player1, new GenePollinator());
        Permanent firstFountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent secondFountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        int pollinatorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pollinator);
        harness.activateAbility(player1, pollinatorIndex, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstFountain.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pollinator.isTapped()).isTrue();
        assertThat(firstFountain.isTapped()).isTrue();
        assertThat(secondFountain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without another untapped permanent")
    void cannotActivateWithoutAnotherUntappedPermanent() {
        Permanent pollinator = addCreatureReady(player1, new GenePollinator());
        int pollinatorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pollinator);

        assertThatThrownBy(() -> harness.activateAbility(player1, pollinatorIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }
}
