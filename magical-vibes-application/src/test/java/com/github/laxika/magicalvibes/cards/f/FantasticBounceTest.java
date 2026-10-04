package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FantasticBounce.class, GrizzlyBears.class, HowlingMine.class, Island.class})
class FantasticBounceTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a nonland permanent and draws a card")
    void returnsNonlandPermanentAndDrawsCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new FantasticBounce()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Costs {1}{U} when targeting a tapped creature")
    void reducedCostWhenTargetingTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new FantasticBounce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Requires the full cost when targeting an untapped creature")
    void fullCostWhenTargetingUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FantasticBounce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new FantasticBounce()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Returns a tapped noncreature permanent at the full cost and draws")
    void returnsTappedArtifactAtFullCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HowlingMine());
        target.tap();
        Card drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new FantasticBounce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Howling Mine");
        harness.assertInHand(player2, "Howling Mine");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("A tapped noncreature does not qualify for the cost reduction")
    void tappedArtifactDoesNotReduceCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HowlingMine());
        target.tap();
        harness.setHand(player1, List.of(new FantasticBounce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A discounted spell still resolves when its own targeted creature untaps")
    void untappingAfterCastingDoesNotPreventResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.tap();
        Card drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new FantasticBounce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not draw when its only target leaves the battlefield")
    void illegalTargetPreventsDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new FantasticBounce()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Fantastic Bounce");
    }

    @Test
    @DisplayName("The discount does not remove the blue mana requirement")
    void discountStillRequiresBlueMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new FantasticBounce()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
