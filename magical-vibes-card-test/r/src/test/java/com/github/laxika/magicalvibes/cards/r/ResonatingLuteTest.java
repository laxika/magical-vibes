package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResonatingLute.class, Forest.class, Island.class})
class ResonatingLuteTest extends BaseCardTest {

    @Test
    @DisplayName("Draw ability cannot be activated with fewer than seven cards in hand")
    void drawAbilityRequiresSevenCards() {
        harness.addToBattlefield(player1, new ResonatingLute());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("7 or more cards in your hand");
    }

    @Test
    @DisplayName("Draw ability works with seven or more cards in hand")
    void drawAbilityWorksWithSevenCards() {
        harness.addToBattlefield(player1, new ResonatingLute());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }

    @Test
    @DisplayName("Lands you control gain a two-mana ability spendable only on instants and sorceries")
    void landsGainRestrictedManaAbility() {
        harness.addToBattlefield(player1, new ResonatingLute());
        harness.addToBattlefield(player1, new Island());

        // Island (battlefield index 1) gains the granted ability at ability index 0.
        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void drawStillResolvesAfterHandDropsBelowSeven() {
        var lute = harness.addToBattlefieldAndReturn(player1, new ResonatingLute());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Island(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(lute.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void grantedManaAbilityTapsLandAndDoesNotUseStack() {
        var lute = harness.addToBattlefieldAndReturn(player1, new ResonatingLute());
        var land = harness.addToBattlefieldAndReturn(player1, new Island());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(land.isTapped()).isTrue();
        assertThat(lute.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.GREEN)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsLandsDoNotGainAbility() {
        harness.addToBattlefield(player1, new ResonatingLute());
        harness.addToBattlefield(player2, new Island());
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player2.getId()).getInstantSorceryOnlyColored(ManaColor.BLUE)).isZero();
    }

    @Test
    void landRetainsItsNormalManaAbility() {
        harness.addToBattlefield(player1, new ResonatingLute());
        harness.addToBattlefield(player1, new Island());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.BLUE)).isZero();
    }

    @Test
    void restrictedManaCannotCastArtifact() {
        harness.addToBattlefield(player1, new ResonatingLute());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new ResonatingLute()));

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.activateAbility(player1, 2, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
