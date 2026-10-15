package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.MonoistSentry;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Honor.class, MonoistSentry.class, Plains.class})
class HonorTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on the target creature and draws a card")
    void putsCounterOnTargetAndDrawsCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MonoistSentry());
        harness.setHand(player1, List.of(new Honor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new Honor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Can target an opponent's creature, but only the caster draws")
    void targetsOpponentsCreatureAndCasterDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MonoistSentry());
        Plains drawnCard = new Plains();
        harness.setLibrary(player1, List.of(drawnCard, new Plains()));
        harness.setHand(player1, List.of(new Honor()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Honor");
    }

    @Test
    @DisplayName("Does not draw when its only target leaves the battlefield")
    void doesNotDrawWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MonoistSentry());
        harness.setHand(player1, List.of(new Honor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        harness.assertInGraveyard(player1, "Honor");
    }
}
