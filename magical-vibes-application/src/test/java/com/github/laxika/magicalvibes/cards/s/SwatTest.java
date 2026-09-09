package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BloatedToad;
import com.github.laxika.magicalvibes.cards.c.Crawlspace;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Swat.class, BloatedToad.class, SimianGrunts.class, Crawlspace.class, MightOfOaks.class})
class SwatTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature with power 2 or less")
    void destroysSmallCreature() {
        Permanent creature = addCreatureReady(player2, new BloatedToad());

        castSwat(creature);

        harness.assertNotOnBattlefield(player2, "Bloated Toad");
        harness.assertInGraveyard(player2, "Bloated Toad");
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetLargeCreature() {
        Permanent creature = addCreatureReady(player2, new SimianGrunts());

        assertThatThrownBy(() -> castSwat(creature))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Crawlspace());

        assertThatThrownBy(() -> castSwat(artifact))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Fizzles when the target's power becomes greater than 2 before resolution")
    void fizzlesWhenTargetBecomesTooPowerful() {
        Permanent creature = addCreatureReady(player2, new BloatedToad());
        harness.setHand(player1, List.of(new Swat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creature.getId());

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new MightOfOaks()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Bloated Toad");
        harness.assertNotInGraveyard(player2, "Bloated Toad");
        harness.assertInGraveyard(player1, "Swat");
    }

    @Test
    @DisplayName("Cycling discards Swat and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Swat()));
        harness.setLibrary(player1, List.of(new BloatedToad()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Swat");
        harness.assertInHand(player1, "Bloated Toad");
    }

    private void castSwat(Permanent target) {
        harness.setHand(player1, List.of(new Swat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
