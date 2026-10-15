package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GatherCourage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimordialSage.class, Forest.class, BirdsOfParadise.class, GatherCourage.class})
class PrimordialSageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature spell lets its controller draw a card")
    void drawsWhenAcceptedAfterCastingCreature() {
        Card drawn = new Forest();
        harness.addToBattlefield(player1, new PrimordialSage());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new BirdsOfParadise()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Declining Primordial Sage's ability does not draw")
    void decliningDoesNotDraw() {
        Card drawn = new Forest();
        harness.addToBattlefield(player1, new PrimordialSage());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new BirdsOfParadise()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
    }

    @Test
    @DisplayName("Casting a noncreature spell does not trigger Primordial Sage")
    void noncreatureSpellDoesNotTrigger() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new PrimordialSage());
        harness.setHand(player1, List.of(new GatherCourage()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, sage.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Casting a creature spell by an opponent does not trigger Primordial Sage")
    void opponentCreatureSpellDoesNotTrigger() {
        Card drawn = new Forest();
        harness.addToBattlefield(player1, new PrimordialSage());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(new BirdsOfParadise()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
    }

    @Test
    @DisplayName("Primordial Sage does not trigger from its own casting")
    void doesNotTriggerForItsOwnCasting() {
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new PrimordialSage()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Primordial Sage");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Each Primordial Sage offers a separate draw before the creature resolves")
    void multipleSagesTriggerIndependently() {
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.addToBattlefield(player1, new PrimordialSage());
        harness.addToBattlefield(player1, new PrimordialSage());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new BirdsOfParadise()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondDraw);
        harness.assertNotOnBattlefield(player1, "Birds of Paradise");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Birds of Paradise");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Primordial Sage triggers for every creature cast in the same turn")
    void triggersForEachCreatureCast() {
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.addToBattlefield(player1, new PrimordialSage());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new BirdsOfParadise(), new BirdsOfParadise()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
