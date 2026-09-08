package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeddingSecurity.class})
class WeddingSecurityTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Blood token on attack puts a counter on Wedding Security and draws a card")
    void sacrificingBloodTokenBoostsAndDraws() {
        Permanent security = addCreatureReady(player1, new WeddingSecurity());
        Permanent blood = addBloodToken();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blood.getId());

        assertThat(security.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blood);
    }

    @Test
    @DisplayName("Declining the Blood sacrifice does nothing")
    void decliningSacrificeDoesNothing() {
        Permanent security = addCreatureReady(player1, new WeddingSecurity());
        Permanent blood = addBloodToken();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(security.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blood);
    }

    @Test
    @DisplayName("Accepting without a Blood token does nothing")
    void noBloodTokenDoesNothing() {
        Permanent security = addCreatureReady(player1, new WeddingSecurity());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(security.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addBloodToken() {
        Card blood = new Card();
        blood.setName("Blood");
        blood.setType(CardType.ARTIFACT);
        blood.setSubtypes(List.of(CardSubtype.BLOOD));
        blood.setToken(true);
        return harness.addToBattlefieldAndReturn(player1, blood);
    }
}
