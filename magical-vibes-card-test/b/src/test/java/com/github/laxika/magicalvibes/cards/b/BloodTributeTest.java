package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.v.VampireNeonate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodTribute.class, VampireNeonate.class})
class BloodTributeTest extends BaseCardTest {

    @Test
    void targetOpponentLosesHalfLifeRoundedUp() {
        harness.setLife(player2, 21);
        harness.setHand(player1, List.of(new BloodTribute()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 10);
        harness.assertLife(player1, 20);
    }

    @Test
    void kickerTapsVampireAndControllerGainsLifeLost() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireNeonate());
        harness.setLife(player2, 21);
        harness.setHand(player1, List.of(new BloodTribute()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castKickedSorceryWithTap(player1, 0, player2.getId(), vampire.getId());
        assertThat(vampire.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        harness.assertLife(player1, 31);
        assertThat(vampire.isTapped()).isTrue();
    }

    @Test
    void cannotTargetController() {
        harness.setHand(player1, List.of(new BloodTribute()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void evenLifeTotalIsHalvedWithoutRoundingUpAnExtraPoint() {
        harness.setHand(player1, List.of(new BloodTribute()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 10);
        harness.assertLife(player1, 20);
    }

    @Test
    void kickedSpellUsesLifeTotalAtResolution() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireNeonate());
        vampire.setSummoningSick(true);
        harness.setHand(player1, List.of(new BloodTribute()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castKickedSorceryWithTap(player1, 0, player2.getId(), vampire.getId());
        harness.setLife(player2, 15);
        harness.passBothPriorities();

        harness.assertLife(player2, 7);
        harness.assertLife(player1, 28);
    }

    @Test
    void vampireLeavingBattlefieldDoesNotUndoPaidKicker() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireNeonate());
        harness.setHand(player1, List.of(new BloodTribute()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castKickedSorceryWithTap(player1, 0, player2.getId(), vampire.getId());
        gd.playerBattlefields.get(player1.getId()).remove(vampire);
        harness.setGraveyard(player1, List.of(vampire.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        harness.assertLife(player1, 30);
    }

    @Test
    void cannotKickByTappingAnAlreadyTappedVampire() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireNeonate());
        vampire.tap();
        harness.setHand(player1, List.of(new BloodTribute()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castKickedSorceryWithTap(
                player1, 0, player2.getId(), vampire.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void cannotKickByTappingOpponentsVampire() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player2, new VampireNeonate());
        harness.setHand(player1, List.of(new BloodTribute()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castKickedSorceryWithTap(
                player1, 0, player2.getId(), vampire.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }
}
