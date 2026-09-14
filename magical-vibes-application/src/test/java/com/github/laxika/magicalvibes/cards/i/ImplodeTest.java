package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CalderaKavu;
import com.github.laxika.magicalvibes.cards.m.MeteorCrater;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Implode.class, MeteorCrater.class, CalderaKavu.class})
class ImplodeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target land and draws a card")
    void destroysTargetLandAndDrawsCard() {
        harness.addToBattlefield(player2, new MeteorCrater());
        harness.setHand(player1, List.of(new Implode()));
        harness.setLibrary(player1, List.of(new CalderaKavu()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Meteor Crater");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Meteor Crater");
        harness.assertInGraveyard(player2, "Meteor Crater");
        harness.assertInHand(player1, "Caldera Kavu");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new CalderaKavu());
        harness.setHand(player1, List.of(new Implode()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID creatureId = harness.getPermanentId(player2, "Caldera Kavu");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles and does not draw if the target leaves before resolution")
    void fizzlesAndDoesNotDrawIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new MeteorCrater());
        harness.setHand(player1, List.of(new Implode()));
        harness.setLibrary(player1, List.of(new CalderaKavu()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Meteor Crater");
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertNotInHand(player1, "Caldera Kavu");
        harness.assertInGraveyard(player1, "Implode");
    }
}
