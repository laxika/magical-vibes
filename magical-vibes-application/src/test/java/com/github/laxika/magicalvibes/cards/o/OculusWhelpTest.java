package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DaybreakRanger;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OculusWhelp.class, DaybreakRanger.class, Shock.class})
class OculusWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it dies while you control a transformed permanent")
    void drawsWhenItDiesWithTransformedPermanent() {
        Permanent whelp = addCreatureReady(player1, new OculusWhelp());
        addTransformedPermanent();
        harness.setLibrary(player1, List.of(new DaybreakRanger()));

        killWithShock(whelp.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when it dies without a transformed permanent")
    void doesNotDrawWithoutTransformedPermanent() {
        Permanent whelp = addCreatureReady(player1, new OculusWhelp());
        harness.setLibrary(player1, List.of(new DaybreakRanger()));

        killWithShock(whelp.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addTransformedPermanent() {
        Permanent ranger = addCreatureReady(player1, new DaybreakRanger());
        ranger.setCard(ranger.getOriginalCard().getBackFaceCard());
        ranger.setTransformed(true);
    }

    private void killWithShock(UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
