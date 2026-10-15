package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KjeldoranOutrider;
import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.cards.w.WhiteShieldCrusader;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sunscour.class, KjeldoranOutrider.class, WhiteShieldCrusader.class, MishrasBauble.class})
class SunscourTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and leaves noncreature permanents alone")
    void destroysAllCreatures() {
        harness.addToBattlefield(player1, new KjeldoranOutrider());
        harness.addToBattlefield(player2, new WhiteShieldCrusader());
        harness.addToBattlefield(player1, new MishrasBauble());
        harness.castFromHand(player1, new Sunscour(), "{5}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kjeldoran Outrider");
        harness.assertNotOnBattlefield(player2, "White Shield Crusader");
        harness.assertOnBattlefield(player1, "Mishra's Bauble");
    }

    @Test
    @DisplayName("Can be cast by exiling two white cards from hand")
    void castsByExilingTwoWhiteCards() {
        harness.addToBattlefield(player2, new KjeldoranOutrider());
        harness.setHand(player1, List.of(new Sunscour(), new KjeldoranOutrider(), new WhiteShieldCrusader()));
        harness.castInstantWithAlternateExileFromHand(player1, 0, (UUID) null, List.of(1, 2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kjeldoran Outrider");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactlyInAnyOrder("Kjeldoran Outrider", "White Shield Crusader");
    }

    @Test
    @DisplayName("Alternate cost requires exactly two matching hand cards")
    void alternateCostRequiresTwoWhiteCards() {
        harness.setHand(player1, List.of(new Sunscour(), new KjeldoranOutrider()));
        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, (UUID) null, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost rejects a non-white hand card")
    void alternateCostRequiresWhiteCards() {
        harness.setHand(player1, List.of(new Sunscour(), new KjeldoranOutrider(), new MishrasBauble()));
        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, (UUID) null, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile the same white card twice to pay the alternate cost")
    void alternateCostRejectsDuplicateCards() {
        harness.setHand(player1, List.of(new Sunscour(), new KjeldoranOutrider(), new WhiteShieldCrusader()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, (UUID) null, List.of(1, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Cannot exile the spell itself to pay its alternate cost")
    void alternateCostRejectsSunscourItself() {
        harness.setHand(player1, List.of(new Sunscour(), new KjeldoranOutrider(), new WhiteShieldCrusader()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, (UUID) null, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Exiles cards on either side of the spell's hand position and leaves other cards alone")
    void alternateCostHandlesSpellInMiddleOfHand() {
        harness.addToBattlefield(player1, new KjeldoranOutrider());
        harness.addToBattlefield(player2, new WhiteShieldCrusader());
        harness.setHand(player1, List.of(new KjeldoranOutrider(), new Sunscour(),
                new WhiteShieldCrusader(), new MishrasBauble()));

        harness.castInstantWithAlternateExileFromHand(player1, 1, (UUID) null, List.of(2, 0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Mishra's Bauble");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactlyInAnyOrder("Kjeldoran Outrider", "White Shield Crusader");
        harness.assertInGraveyard(player1, "Kjeldoran Outrider");
        harness.assertInGraveyard(player2, "White Shield Crusader");
        harness.assertInGraveyard(player1, "Sunscour");
    }
}
