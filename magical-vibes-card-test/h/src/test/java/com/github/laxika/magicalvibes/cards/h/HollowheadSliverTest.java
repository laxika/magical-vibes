package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HollowheadSliver.class, MetallicSliver.class, GrizzlyBears.class})
class HollowheadSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Grants the rummage ability to Sliver creatures you control")
    void grantsAbilityToOwnSliversOnly() {
        Permanent source = addCreatureReady(player1, new HollowheadSliver());
        Permanent ownSliver = addCreatureReady(player1, new MetallicSliver());
        Permanent opposingSliver = addCreatureReady(player2, new MetallicSliver());
        Permanent nonSliver = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gs.getEffectiveActivatedAbilities(gd, source)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, nonSliver)).isEmpty();
    }

    @Test
    @DisplayName("A Sliver can tap and discard a card to draw a card")
    void tapsDiscardsAndDraws() {
        Permanent source = addCreatureReady(player1, new HollowheadSliver());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new MetallicSliver()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Metallic Sliver");
    }

    @Test
    @DisplayName("The ability cannot be activated without a card to discard")
    void requiresCardToDiscard() {
        addCreatureReady(player1, new HollowheadSliver());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A granted ability pays its costs before drawing on resolution")
    void grantedAbilityPaysCostsBeforeResolution() {
        addCreatureReady(player1, new HollowheadSliver());
        Permanent sliver = addCreatureReady(player1, new MetallicSliver());
        harness.setHand(player1, List.of(new HollowheadSliver()));
        harness.setLibrary(player1, List.of(new MetallicSliver()));
        harness.setHand(player2, List.of(new HollowheadSliver()));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(sliver.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Hollowhead Sliver");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.assertInHand(player2, "Hollowhead Sliver");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Metallic Sliver");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player2, "Hollowhead Sliver");
    }

    @Test
    @DisplayName("Summoning sickness prevents using the granted tap ability")
    void summoningSickSliverCannotActivate() {
        addCreatureReady(player1, new HollowheadSliver());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new MetallicSliver());
        sliver.setSummoningSick(true);
        harness.setHand(player1, List.of(new HollowheadSliver()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(sliver.isTapped()).isFalse();
        harness.assertInHand(player1, "Hollowhead Sliver");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped Sliver cannot pay the tap cost")
    void tappedSliverCannotActivate() {
        Permanent sliver = addCreatureReady(player1, new HollowheadSliver());
        sliver.tap();
        harness.setHand(player1, List.of(new HollowheadSliver()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertInHand(player1, "Hollowhead Sliver");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Slivers lose the granted ability when Hollowhead Sliver leaves")
    void grantEndsWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new HollowheadSliver());
        Permanent sliver = addCreatureReady(player1, new MetallicSliver());
        harness.setHand(player1, List.of(new HollowheadSliver()));

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);

        assertThat(gs.getEffectiveActivatedAbilities(gd, sliver)).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sliver.isTapped()).isFalse();
        harness.assertInHand(player1, "Hollowhead Sliver");
    }

    @Test
    @DisplayName("An activated granted ability still resolves after the granting Sliver leaves")
    void activatedAbilitySurvivesLossOfGrant() {
        Permanent source = addCreatureReady(player1, new HollowheadSliver());
        Permanent sliver = addCreatureReady(player1, new MetallicSliver());
        harness.setHand(player1, List.of(new HollowheadSliver()));
        harness.setLibrary(player1, List.of(new MetallicSliver()));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);

        assertThat(gs.getEffectiveActivatedAbilities(gd, sliver)).isEmpty();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Metallic Sliver");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(sliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted ability can be used during an opponent's turn")
    void canActivateDuringOpponentsTurn() {
        addCreatureReady(player1, new HollowheadSliver());
        Permanent sliver = addCreatureReady(player1, new MetallicSliver());
        harness.setHand(player1, List.of(new HollowheadSliver()));
        harness.setLibrary(player1, List.of(new MetallicSliver()));
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(sliver.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Hollowhead Sliver");
        harness.assertInHand(player1, "Metallic Sliver");
    }

}
