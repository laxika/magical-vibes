package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FadeIntoAntiquity;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TamiyosCompleation;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircuitMender.class, Plains.class, WrathOfGod.class, FadeIntoAntiquity.class,
        ColossalSkyturtle.class, TamiyosCompleation.class})
class CircuitMenderTest extends BaseCardTest {

    @Test
    @DisplayName("Circuit Mender's enters-the-battlefield ability gains 2 life")
    void entersBattlefieldGainsLife() {
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new CircuitMender(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Circuit Mender's leaves-the-battlefield ability draws a card")
    void leavesBattlefieldDrawsCard() {
        harness.addToBattlefield(player1, new CircuitMender());
        harness.setLibrary(player1, List.of(new Plains()));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Circuit Mender");
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Exiling Circuit Mender draws for its controller after the trigger resolves")
    void exileDrawsForController() {
        var mender = harness.addToBattlefieldAndReturn(player2, new CircuitMender());
        var drawnCard = new Plains();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player1, List.of(new FadeIntoAntiquity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, mender.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Circuit Mender");
        assertThat(gd.findExiledCard(mender.getCard().getId())).isNotNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        harness.assertNotInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Returning Circuit Mender to hand draws a card without gaining life")
    void returnToHandDrawsCard() {
        var mender = harness.addToBattlefieldAndReturn(player2, new CircuitMender());
        var drawnCard = new Plains();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player1, List.of(new ColossalSkyturtle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player2.getId());
        harness.ensurePriority(player1);

        gs.activateHandAbility(gd, player1, 0, 1, mender.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Circuit Mender");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(mender.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(mender.getCard(), drawnCard);
        harness.assertLife(player2, lifeBefore);
        harness.assertNotInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Circuit Mender does not draw when it leaves after losing all abilities")
    void noDrawAfterLosingAbilities() {
        var mender = harness.addToBattlefieldAndReturn(player2, new CircuitMender());
        var libraryCard = new Plains();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new TamiyosCompleation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, mender.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new FadeIntoAntiquity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, mender.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Circuit Mender");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
    }
}
