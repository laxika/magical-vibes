package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrandBallGuest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlickACoin.class, GrandBallGuest.class})
class FlickACoinTest extends BaseCardTest {

    @Test
    void dealsDamageToCreatureCreatesTreasureAndDrawsCard() {
        harness.addToBattlefield(player2, new GrandBallGuest());
        harness.setHand(player1, List.of(new FlickACoin()));
        harness.setLibrary(player1, List.of(new GrandBallGuest()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grand Ball Guest"));

        Permanent creature = findPermanent(player2, "Grand Ball Guest");
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertInHand(player1, "Grand Ball Guest");
    }

    @Test
    void dealsDamageToPlayerCreatesTreasureAndDrawsCard() {
        harness.setHand(player1, List.of(new FlickACoin()));
        harness.setLibrary(player1, List.of(new GrandBallGuest()));
        harness.addMana(player1, ManaColor.RED, 3);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertInHand(player1, "Grand Ball Guest");
    }

    @Test
    void illegalTargetPreventsTreasureAndCardDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrandBallGuest());
        harness.setHand(player1, List.of(new FlickACoin()));
        harness.setLibrary(player1, List.of(new GrandBallGuest()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertNotInHand(player1, "Grand Ball Guest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Flick a Coin");
    }

    @Test
    void lethalDamageStillCreatesTreasureAndDrawsExactlyOneCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrandBallGuest());
        target.setMarkedDamage(1);
        harness.setHand(player1, List.of(new FlickACoin()));
        harness.setLibrary(player1, List.of(new GrandBallGuest(), new FlickACoin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grand Ball Guest");
        harness.assertInGraveyard(player2, "Grand Ball Guest");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Flick a Coin");
    }

    @Test
    void canTargetControllerAndRewardsStillGoToController() {
        harness.setHand(player1, List.of(new FlickACoin()));
        harness.setLibrary(player1, List.of(new GrandBallGuest()));
        harness.addMana(player1, ManaColor.RED, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, lifeBefore - 1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertInHand(player1, "Grand Ball Guest");
    }
}
