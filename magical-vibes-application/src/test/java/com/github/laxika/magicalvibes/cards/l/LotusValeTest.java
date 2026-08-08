package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LotusValeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices two chosen untapped lands and the land enters")
    void entersBySacrificingTwoUntappedLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        List<Permanent> lands = gd.playerBattlefields.get(player1.getId());
        harness.setHand(player1, List.of(new LotusVale()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, lands.stream().map(Permanent::getId).toList());

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Lotus Vale");
    }

    @Test
    @DisplayName("Choosing fewer than two lands is rejected, while choosing none declines")
    void mustChooseExactlyTwoLandsOrDecline() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        List<Permanent> lands = gd.playerBattlefields.get(player1.getId());
        harness.setHand(player1, List.of(new LotusVale()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1,
                List.of(lands.getFirst().getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Lotus Vale");
        harness.assertInGraveyard(player1, "Lotus Vale");
    }

    @Test
    @DisplayName("Declining the sacrifice puts Lotus Vale into its owner's graveyard")
    void decliningSacrificeSendsLandToGraveyard() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new LotusVale()));

        harness.playLand(player1, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Lotus Vale");
        harness.assertInGraveyard(player1, "Lotus Vale");
    }

    @Test
    @DisplayName("With fewer than two untapped lands Lotus Vale goes straight to the graveyard")
    void insufficientUntappedLandsSendLandToGraveyard() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        gd.playerBattlefields.get(player1.getId()).getLast().tap();
        harness.setHand(player1, List.of(new LotusVale()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Lotus Vale");
        harness.assertInGraveyard(player1, "Lotus Vale");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Tapping Lotus Vale adds three mana of the chosen color")
    void manaAbilityAddsThreeManaOfChosenColor() {
        harness.addToBattlefield(player1, new LotusVale());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }
}
