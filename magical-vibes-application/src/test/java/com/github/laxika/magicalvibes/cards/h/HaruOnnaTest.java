package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CallousDeceiver;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HaruOnnaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws a card")
    void entersAndDrawsCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new HaruOnna()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Haru-Onna");
    }

    @Test
    @DisplayName("Casting a Spirit spell may return Haru-Onna to its owner's hand")
    void spiritSpellReturnsHaruOnna() {
        addHaruOnna();
        harness.setHand(player1, List.of(new CallousDeceiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Haru-Onna");
    }

    @Test
    @DisplayName("Casting an Arcane spell may return Haru-Onna to its owner's hand")
    void arcaneSpellReturnsHaruOnna() {
        addHaruOnna();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Haru-Onna");
    }

    @Test
    @DisplayName("Declining the cast trigger leaves Haru-Onna on the battlefield")
    void decliningCastTriggerLeavesHaruOnnaOnBattlefield() {
        addHaruOnna();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Haru-Onna");
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger Haru-Onna")
    void unrelatedSpellDoesNotTrigger() {
        addHaruOnna();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        harness.assertOnBattlefield(player1, "Haru-Onna");
    }

    private void addHaruOnna() {
        harness.addToBattlefield(player1, new HaruOnna());
    }
}
