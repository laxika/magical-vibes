package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.c.CribSwap;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GoblinSledder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraveSifter.class, ElvishWarrior.class, GoblinSledder.class, AvianChangeling.class, CribSwap.class})
class GraveSifterTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses a type and may return any number of matching creatures")
    void eachPlayerChoosesTypeAndReturnsAnyNumber() {
        harness.setGraveyard(player1, List.of(new ElvishWarrior(), new ElvishWarrior(), new GoblinSledder()));
        harness.setGraveyard(player2, List.of(new GoblinSledder(), new ElvishWarrior()));
        cast();

        assertThat(harnessColorChoicePlayer()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, "ELF");
        assertThat(harnessColorChoicePlayer()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, "GOBLIN");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).validIndices())
                .containsExactly(0, 1);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleGraveyardCardChosen(player2, 0);

        harness.assertInHand(player1, "Elvish Warrior");
        harness.assertInHand(player2, "Goblin Sledder");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Elvish Warrior", "Goblin Sledder");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Elvish Warrior");
    }

    @Test
    @DisplayName("Cards with creature types match and players may return zero cards")
    void changelingMatchesAndReturningZeroIsAllowed() {
        harness.setGraveyard(player1, List.of(new CribSwap()));
        harness.setGraveyard(player2, List.of(new GoblinSledder(), new AvianChangeling()));
        cast();

        harness.handleListChoice(player1, "SHAPESHIFTER");
        harness.handleListChoice(player2, "GIANT");
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player2, -1);

        harness.assertInHand(player1, "Crib Swap");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .doesNotContain("Goblin Sledder", "Avian Changeling");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Goblin Sledder", "Avian Changeling");
    }

    private void cast() {
        harness.setHand(player1, List.of(new GraveSifter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private java.util.UUID harnessColorChoicePlayer() {
        return gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId();
    }
}
