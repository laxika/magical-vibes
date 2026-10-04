package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArashinCleric;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FriendlyFire.class, GrizzlyBears.class, Mountain.class, ArashinCleric.class})
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
        harness.castAndResolveInstant(player1, 0, targetId);

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
        harness.castAndResolveInstant(player1, 0, targetId);

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

    @Test
    @DisplayName("Lethal creature damage still deals the full revealed mana value to its controller")
    void lethalDamageAlsoDamagesController() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new ArashinCleric()).getId();
        harness.setHand(player2, List.of(new FriendlyFire()));
        harness.setHand(player1, List.of(new FriendlyFire()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Arashin Cleric");
        harness.assertNotOnBattlefield(player2, "Arashin Cleric");
        harness.assertLife(player2, 16);
        harness.assertInHand(player2, "Friendly Fire");
    }

    @Test
    @DisplayName("A revealed land deals no damage")
    void revealedLandDealsNoDamage() {
        var target = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        harness.setHand(player2, List.of(new Mountain()));
        harness.setHand(player1, List.of(new FriendlyFire()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Mountain");
    }

    @Test
    @DisplayName("Can target your own creature and reveals from your hand after casting")
    void targetsOwnCreatureAndUsesControllersHand() {
        var target = harness.addToBattlefieldAndReturn(player1, new ArashinCleric());
        harness.setHand(player1, List.of(new FriendlyFire(), new ArashinCleric()));
        harness.setHand(player2, List.of(new FriendlyFire()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Arashin Cleric");
    }

    @Test
    @DisplayName("Does not damage the controller if the only target has left the battlefield")
    void removedTargetPreventsPlayerDamage() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new ArashinCleric()).getId();
        harness.setHand(player2, List.of(new FriendlyFire()));
        harness.setHand(player1, List.of(new FriendlyFire()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Friendly Fire");
        harness.assertInGraveyard(player1, "Friendly Fire");
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
