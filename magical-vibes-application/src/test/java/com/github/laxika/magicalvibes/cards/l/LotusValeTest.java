package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
import com.github.laxika.magicalvibes.cards.x.XanthicStatue;
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

@CardUsed({LotusVale.class, WindingCanyons.class, XanthicStatue.class})
class LotusValeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices two chosen untapped lands and the land enters")
    void entersBySacrificingTwoUntappedLands() {
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.addToBattlefield(player1, new WindingCanyons());
        List<Permanent> lands = findPermanents(player1, "Winding Canyons");
        harness.setHand(player1, List.of(new LotusVale()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, lands.stream().map(Permanent::getId).toList());

        harness.assertNotOnBattlefield(player1, "Winding Canyons");
        harness.assertOnBattlefield(player1, "Lotus Vale");
    }

    @Test
    @DisplayName("Choosing fewer than two lands is rejected, while choosing none declines")
    void mustChooseExactlyTwoLandsOrDecline() {
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.addToBattlefield(player1, new WindingCanyons());
        List<Permanent> lands = findPermanents(player1, "Winding Canyons");
        harness.setHand(player1, List.of(new LotusVale()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1,
                List.of(lands.getFirst().getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player1, "Winding Canyons");
        harness.assertNotOnBattlefield(player1, "Lotus Vale");
        harness.assertInGraveyard(player1, "Lotus Vale");
    }

    @Test
    @DisplayName("Declining the sacrifice puts Lotus Vale into its owner's graveyard")
    void decliningSacrificeSendsLandToGraveyard() {
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.setHand(player1, List.of(new LotusVale()));

        harness.playLand(player1, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player1, "Winding Canyons");
        harness.assertNotOnBattlefield(player1, "Lotus Vale");
        harness.assertInGraveyard(player1, "Lotus Vale");
    }

    @Test
    @DisplayName("With fewer than two untapped lands Lotus Vale goes straight to the graveyard")
    void insufficientUntappedLandsSendLandToGraveyard() {
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.addToBattlefield(player1, new WindingCanyons());
        findPermanents(player1, "Winding Canyons").getLast().tap();
        harness.setHand(player1, List.of(new LotusVale()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Lotus Vale");
        harness.assertInGraveyard(player1, "Lotus Vale");
        harness.assertOnBattlefield(player1, "Winding Canyons");
    }

    @Test
    @DisplayName("With only a nonland and one land, Lotus Vale goes to the graveyard")
    void nonlandsCannotBeSacrificedAsLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WindingCanyons());
        Permanent nonland = harness.addToBattlefieldAndReturn(player1, new XanthicStatue());
        LotusVale lotusVale = new LotusVale();
        harness.setHand(player1, List.of(lotusVale));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(land, nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lotusVale);
    }

    @Test
    @DisplayName("Lands controlled by an opponent cannot be sacrificed")
    void cannotSacrificeOpponentsLands() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        LotusVale lotusVale = new LotusVale();
        harness.setHand(player1, List.of(lotusVale));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(firstLand, secondLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lotusVale);
    }

    @Test
    @DisplayName("A Lotus Vale that is not owned by its controller goes to its owner's graveyard")
    void failedEntryUsesOwnersGraveyard() {
        LotusVale lotusVale = new LotusVale();
        lotusVale.setOwnerId(player2.getId());
        harness.setHand(player1, List.of(lotusVale));

        harness.playLand(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(lotusVale);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(lotusVale);
    }

    @Test
    @DisplayName("Tapping Lotus Vale adds three mana of the chosen color")
    void manaAbilityAddsThreeManaOfChosenColor() {
        harness.addToBattlefield(player1, new LotusVale());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        Permanent land = findPermanent(player1, "Lotus Vale");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }
}
