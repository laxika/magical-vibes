package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.n.NantukoMonastery;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmberShot.class, GiantWarthog.class, NantukoMonastery.class, SuntailHawk.class})
class EmberShotTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a creature and draws a card")
    void damagesCreatureAndDrawsCard() {
        harness.addToBattlefield(player2, new GiantWarthog());
        harness.setHand(player1, List.of(new EmberShot()));
        harness.setLibrary(player1, List.of(new GiantWarthog()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Giant Warthog"));

        assertThat(findPermanent(player2, "Giant Warthog").getMarkedDamage()).isEqualTo(3);
        harness.assertInHand(player1, "Giant Warthog");
    }

    @Test
    @DisplayName("Deals 3 damage to a player and draws a card")
    void damagesPlayerAndDrawsCard() {
        harness.setHand(player1, List.of(new EmberShot()));
        harness.setLibrary(player1, List.of(new GiantWarthog()));
        harness.addMana(player1, ManaColor.RED, 7);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        harness.assertInHand(player1, "Giant Warthog");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new NantukoMonastery());
        harness.setHand(player1, List.of(new EmberShot()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Nantuko Monastery")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");
    }

    @Test
    @DisplayName("Lethal damage to a creature still draws exactly one card")
    void lethalDamageStillDrawsOneCard() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new EmberShot()));
        harness.setLibrary(player1, List.of(new GiantWarthog(), new NantukoMonastery()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Suntail Hawk"));

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");
        harness.assertInGraveyard(player1, "Ember Shot");
        harness.assertInHand(player1, "Giant Warthog");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotInHand(player2, "Giant Warthog");
    }

    @Test
    @DisplayName("Does not draw when its only target dies before resolution")
    void doesNotDrawWhenTargetDiesBeforeResolution() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new EmberShot()));
        harness.setHand(player2, List.of(new EmberShot()));
        harness.setLibrary(player1, List.of(new GiantWarthog()));
        harness.setLibrary(player2, List.of(new NantukoMonastery()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.addMana(player2, ManaColor.RED, 7);
        var targetId = harness.getPermanentId(player2, "Suntail Hawk");

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, targetId);

        harness.assertInGraveyard(player2, "Suntail Hawk");
        harness.assertInHand(player2, "Nantuko Monastery");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ember Shot");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
