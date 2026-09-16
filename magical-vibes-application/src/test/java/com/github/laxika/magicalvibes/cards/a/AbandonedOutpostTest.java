package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbandonedOutpost.class})
class AbandonedOutpostTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new AbandonedOutpost()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Abandoned Outpost").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one white mana")
    void tapAddsWhiteMana() {
        harness.addToBattlefield(player1, new AbandonedOutpost());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Abandoned Outpost");
    }

    @Test
    @DisplayName("Both mana abilities require an untapped Abandoned Outpost")
    void manaAbilitiesRequireUntappedSource() {
        harness.addToBattlefield(player1, new AbandonedOutpost());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Abandoned Outpost");
    }

    @Test
    @DisplayName("Sacrifice ability adds mana of the chosen color and moves the land to the graveyard")
    void sacrificeAddsChosenColorMana() {
        harness.addToBattlefield(player1, new AbandonedOutpost());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Abandoned Outpost");
        harness.assertInGraveyard(player1, "Abandoned Outpost");
    }

    @Test
    @DisplayName("Sacrifice ability pays its costs before the mana color is chosen")
    void sacrificeCostIsPaidBeforeColorChoice() {
        harness.addToBattlefield(player1, new AbandonedOutpost());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertNotOnBattlefield(player1, "Abandoned Outpost");
        harness.assertInGraveyard(player1, "Abandoned Outpost");

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
