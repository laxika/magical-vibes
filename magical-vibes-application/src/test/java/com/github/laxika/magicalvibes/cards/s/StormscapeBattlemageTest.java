package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StormscapeBattlemageTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, neither ability resolves")
    void noKicker() {
        harness.setHand(player1, List.of(new StormscapeBattlemage()));
        addMana(2, ManaColor.BLUE);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stormscape Battlemage");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("White kicker gains 3 life")
    void whiteKicker() {
        harness.setHand(player1, List.of(new StormscapeBattlemage()));
        addMana(2, ManaColor.BLUE, ManaColor.WHITE);

        castWithAdditionalCosts(List.of("{W}"), null, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Black kicker destroys a target nonblack creature without regeneration")
    void blackKicker() {
        Permanent target = addTarget(new GrizzlyBears());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new StormscapeBattlemage()));
        addMana(4, ManaColor.BLUE, ManaColor.BLACK);

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Both kicker costs resolve their independent abilities")
    void bothKickers() {
        Permanent target = addTarget(new GrizzlyBears());
        harness.setHand(player1, List.of(new StormscapeBattlemage()));
        addMana(4, ManaColor.BLUE, ManaColor.BLACK, ManaColor.WHITE);

        castWithAdditionalCosts(List.of("{W}"), target.getId(), true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Black kicker cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent target = addTarget(new MassOfGhouls());
        harness.setHand(player1, List.of(new StormscapeBattlemage()));
        addMana(4, ManaColor.BLUE, ManaColor.BLACK);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack");
    }

    private Permanent addTarget(Card card) {
        return harness.addToBattlefieldAndReturn(player2, card);
    }

    private void addMana(int colorless, ManaColor... colored) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        for (ManaColor color : colored) {
            harness.addMana(player1, color, 1);
        }
    }

    private void castWithAdditionalCosts(List<String> payments, java.util.UUID targetId, boolean kicked) {
        gs.playCard(gd, player1, 0, 0, targetId, null, List.of(), List.of(), false,
                null, null, null, null, null, kicked, null, null, null, null,
                payments, false);
    }
}
