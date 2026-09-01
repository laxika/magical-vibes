package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FriendlyFireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals the revealed card's mana value to the creature and its controller")
    void dealsRevealedManaValueToCreatureAndController() {
        Card targetCard = createCreature("Large Beast", 4, 5);
        harness.addToBattlefield(player2, targetCard);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new FriendlyFire()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Large Beast");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement()
                .extracting(permanent -> permanent.getMarkedDamage())
                .isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does nothing when the target creature's controller has no cards")
    void emptyHandDealsNoDamage() {
        Card targetCard = createCreature("Large Beast", 4, 5);
        harness.addToBattlefield(player2, targetCard);
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new FriendlyFire()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Large Beast");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement()
                .extracting(permanent -> permanent.getMarkedDamage())
                .isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new FriendlyFire()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private static Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
