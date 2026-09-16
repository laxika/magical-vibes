package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcceptableLosses.class, AvenFlock.class, Forest.class})
class AcceptableLossesTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a random card and deals 5 damage to target creature")
    void discardsAndDealsDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenFlock());
        harness.setHand(player1, List.of(new AcceptableLosses(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        harness.assertInGraveyard(player1, "Acceptable Losses");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @CardUsed(AshenFirebeast.class)
    @DisplayName("Deals exactly 5 damage to a creature that survives")
    void dealsExactlyFiveDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshenFirebeast());
        harness.setHand(player1, List.of(new AcceptableLosses(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot cast without another card in hand for the random discard")
    void cannotCastWithoutAnotherCardInHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenFlock());
        harness.setHand(player1, List.of(new AcceptableLosses()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new AcceptableLosses(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
