package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({ScorchedRuins.class, WindingCanyons.class, XanthicStatue.class})
class ScorchedRuinsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices two chosen untapped lands and the land enters")
    void entersBySacrificingTwoUntappedLands() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new WindingCanyons());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new WindingCanyons());
        harness.setHand(player1, List.of(new ScorchedRuins()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId(), secondLand.getId()));

        harness.assertNotOnBattlefield(player1, "Winding Canyons");
        harness.assertOnBattlefield(player1, "Scorched Ruins");
        assertThat(findPermanent(player1, "Scorched Ruins").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Choosing fewer than two lands is rejected, while choosing none declines")
    void mustChooseExactlyTwoLandsOrDecline() {
        List<Permanent> lands = List.of(
                harness.addToBattlefieldAndReturn(player1, new WindingCanyons()),
                harness.addToBattlefieldAndReturn(player1, new WindingCanyons()));
        harness.setHand(player1, List.of(new ScorchedRuins()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1,
                List.of(lands.getFirst().getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player1, "Winding Canyons");
        harness.assertNotOnBattlefield(player1, "Scorched Ruins");
        harness.assertInGraveyard(player1, "Scorched Ruins");
    }

    @Test
    @DisplayName("With fewer than two untapped lands Scorched Ruins goes straight to the graveyard")
    void insufficientUntappedLandsSendLandToGraveyard() {
        harness.addToBattlefield(player1, new WindingCanyons());
        harness.addToBattlefieldAndReturn(player1, new WindingCanyons()).tap();
        harness.setHand(player1, List.of(new ScorchedRuins()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Scorched Ruins");
        harness.assertInGraveyard(player1, "Scorched Ruins");
        harness.assertOnBattlefield(player1, "Winding Canyons");
    }

    @Test
    @DisplayName("With only a nonland and one land, Scorched Ruins goes to the graveyard")
    void nonlandsCannotBeSacrificedAsLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WindingCanyons());
        Permanent nonland = harness.addToBattlefieldAndReturn(player1, new XanthicStatue());
        ScorchedRuins scorchedRuins = new ScorchedRuins();
        harness.setHand(player1, List.of(scorchedRuins));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(land, nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(scorchedRuins);
    }

    @Test
    @DisplayName("Lands controlled by an opponent cannot be sacrificed")
    void cannotSacrificeOpponentsLands() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());
        ScorchedRuins scorchedRuins = new ScorchedRuins();
        harness.setHand(player1, List.of(scorchedRuins));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(firstLand, secondLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(scorchedRuins);
    }

    @Test
    @DisplayName("A failed entry puts Scorched Ruins into its owner's graveyard")
    void failedEntryUsesOwnersGraveyard() {
        ScorchedRuins scorchedRuins = new ScorchedRuins();
        scorchedRuins.setOwnerId(player2.getId());
        harness.setHand(player1, List.of(scorchedRuins));

        harness.playLand(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(scorchedRuins);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(scorchedRuins);
    }

    @Test
    @DisplayName("Tapping Scorched Ruins adds four colorless mana")
    void manaAbilityAddsFourColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ScorchedRuins());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }
}
