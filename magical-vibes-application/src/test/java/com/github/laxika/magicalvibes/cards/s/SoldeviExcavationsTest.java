package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoldeviExcavationsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices a chosen untapped Island and the land enters")
    void entersBySacrificingUntappedIsland() {
        harness.addToBattlefield(player1, new Island());
        Permanent island = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new SoldeviExcavations()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, island.getId());

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInGraveyard(player1, "Island");
        harness.assertOnBattlefield(player1, "Soldevi Excavations");
    }

    @Test
    @DisplayName("Declining the sacrifice puts the land into its owner's graveyard")
    void declinedSacrificeSendsLandToGraveyard() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new SoldeviExcavations()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, player1.getId());

        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Soldevi Excavations");
        harness.assertInGraveyard(player1, "Soldevi Excavations");
    }

    @Test
    @DisplayName("With no untapped Island the land goes straight to the graveyard without a prompt")
    void noUntappedIslandSendsLandToGraveyard() {
        harness.addToBattlefield(player1, new Island());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.setHand(player1, List.of(new SoldeviExcavations()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Soldevi Excavations");
        harness.assertInGraveyard(player1, "Soldevi Excavations");
    }

    @Test
    @DisplayName("Mana ability adds {C} and {U}")
    void manaAbilityAddsColorlessAndBlue() {
        harness.addToBattlefield(player1, new SoldeviExcavations());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Scry ability enters the scry interaction with one card")
    void scryAbilityEntersScryState() {
        harness.addToBattlefield(player1, new SoldeviExcavations());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }
}
