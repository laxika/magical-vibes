package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({StiltzkinMoogleMerchant.class, Forest.class})
class StiltzkinMoogleMerchantTest extends BaseCardTest {

    @Test
    @DisplayName("Transfers another permanent to an opponent and draws a card")
    void transfersPermanentAndDraws() {
        Permanent stiltzkin = addCreatureReady(player1, new StiltzkinMoogleMerchant());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stiltzkin),
                0,
                List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(stiltzkin.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Cannot target Stiltzkin itself")
    void cannotTargetSourcePermanent() {
        Permanent stiltzkin = addCreatureReady(player1, new StiltzkinMoogleMerchant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stiltzkin),
                0,
                List.of(player2.getId(), stiltzkin.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another permanent you control");
    }
}
