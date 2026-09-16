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

}
