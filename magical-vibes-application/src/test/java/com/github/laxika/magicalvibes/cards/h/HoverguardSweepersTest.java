package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.cards.p.PentadPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoverguardSweepers.class, DrossCrocodile.class, PentadPrism.class})
class HoverguardSweepersTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two target creatures to their owners' hands")
    void returnsTwoCreatures() {
        Permanent crocodile1 = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        Permanent crocodile2 = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        castHoverguardSweepers(List.of(crocodile1.getId(), crocodile2.getId()));
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Dross Crocodile");
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Dross Crocodile"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Can return only one target creature")
    void returnsOneCreature() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        castHoverguardSweepers(List.of(crocodile.getId()));
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Dross Crocodile");
        harness.assertInHand(player2, "Dross Crocodile");
    }

    @Test
    @DisplayName("Can choose no creatures")
    void returnsNoCreatures() {
        castHoverguardSweepers(List.of());

        harness.assertOnBattlefield(player1, "Hoverguard Sweepers");
    }

    @Test
    @DisplayName("Can decline returning targeted creatures")
    void declinesReturningTargetedCreatures() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        castHoverguardSweepers(List.of(crocodile.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Dross Crocodile");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent prism = harness.addToBattlefieldAndReturn(player2, new PentadPrism());
        harness.setHand(player1, List.of(new HoverguardSweepers()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(prism.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castHoverguardSweepers(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new HoverguardSweepers()));
        addMana();

        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
