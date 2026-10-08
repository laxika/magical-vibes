package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.b.BrimstoneVolley;
import com.github.laxika.magicalvibes.cards.b.BumpInTheNight;
import com.github.laxika.magicalvibes.cards.r.RakshasaVizier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CharmbreakerDevils.class, BrimstoneVolley.class, BumpInTheNight.class, DarkthicketWolf.class, RakshasaVizier.class})
class CharmbreakerDevilsTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep returns an instant from graveyard to hand at random")
    void upkeepReturnsInstantFromGraveyard() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());
        BrimstoneVolley volley = new BrimstoneVolley();
        harness.setGraveyard(player1, List.of(volley));

        advanceToUpkeep(player1);

        // Trigger is on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve trigger — random return (no graveyard choice prompt since it's random)
        harness.passBothPriorities();

        harness.assertInHand(player1, "Brimstone Volley");
        harness.assertNotInGraveyard(player1, "Brimstone Volley");
    }

    @Test
    @DisplayName("Upkeep returns a random card when multiple instants/sorceries in graveyard")
    void upkeepReturnsRandomFromMultipleInstantsSorceries() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());
        harness.setGraveyard(player1, List.of(new BrimstoneVolley(), new BumpInTheNight()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // One of the two should be returned to hand
        long handSpells = gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Brimstone Volley") || c.getName().equals("Bump in the Night"))
                .count();
        assertThat(handSpells).isEqualTo(1);

        // One should remain in graveyard
        long graveyardSpells = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Brimstone Volley") || c.getName().equals("Bump in the Night"))
                .count();
        assertThat(graveyardSpells).isEqualTo(1);
    }

    @Test
    @DisplayName("No effect when graveyard has no instants or sorceries")
    void noEffectWithNoInstantsOrSorceries() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());
        harness.setGraveyard(player1, List.of(new DarkthicketWolf()));

        advanceToUpkeep(player1);

        // Trigger fires but should resolve without returning anything
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Darkthicket Wolf");
        harness.assertInGraveyard(player1, "Darkthicket Wolf");
    }

    @Test
    @DisplayName("No effect when graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        // Should resolve without error — no card returned to hand
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Upkeep trigger ignores creature cards and only returns instant/sorcery")
    void upkeepIgnoresCreaturesInGraveyard() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());
        harness.setGraveyard(player1, List.of(new DarkthicketWolf(), new BrimstoneVolley()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Brimstone Volley should be returned (it's the only instant/sorcery)
        harness.assertInHand(player1, "Brimstone Volley");
        // Darkthicket Wolf should stay in graveyard
        harness.assertInGraveyard(player1, "Darkthicket Wolf");
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during opponent's upkeep")
    void upkeepTriggerDoesNotFireDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());
        harness.setGraveyard(player1, List.of(new BrimstoneVolley()));

        advanceToUpkeep(player2);

        // No trigger should fire
        harness.assertInGraveyard(player1, "Brimstone Volley");
        harness.assertNotInHand(player1, "Brimstone Volley");
    }

    @Test
    @DisplayName("Casting an instant spell gives +4/+0 until end of turn")
    void castingInstantGivesBoost() {
        Permanent devils = addCreatureReady(player1, new CharmbreakerDevils());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player1, List.of(new BrimstoneVolley()));

        // Brimstone Volley targets any target — target opponent
        harness.castInstant(player1, 0, player2.getId());

        // Spell cast trigger fires — resolve the +4/+0 boost
        harness.passBothPriorities();

        assertThat(devils.getPowerModifier()).isEqualTo(4);
        assertThat(devils.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a non-instant/sorcery spell does not give +4/+0")
    void castingCreatureDoesNotGiveBoost() {
        Permanent devils = addCreatureReady(player1, new CharmbreakerDevils());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new DarkthicketWolf()));

        harness.castCreature(player1, 0);
        // No spell cast trigger — resolve Darkthicket Wolf
        harness.passBothPriorities();

        assertThat(devils.getPowerModifier()).isEqualTo(0);
        assertThat(devils.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple instant/sorcery casts stack the boost")
    void multipleInstantCastsStackBoost() {
        Permanent devils = addCreatureReady(player1, new CharmbreakerDevils());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast first instant
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.castInstant(player1, 0, player2.getId());

        // Resolve spell cast trigger (+4/+0)
        harness.passBothPriorities();
        assertThat(devils.getPowerModifier()).isEqualTo(4);

        // Resolve Brimstone Volley
        harness.passBothPriorities();

        // Cast second instant
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.castInstant(player1, 0, player2.getId());

        // Resolve second spell cast trigger (+4/+0 again)
        harness.passBothPriorities();
        assertThat(devils.getPowerModifier()).isEqualTo(8);
    }

    @Test
    @DisplayName("Upkeep returns a sorcery from the controller's graveyard only")
    void upkeepReturnsSorceryFromControllersGraveyard() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());
        harness.setGraveyard(player1, List.of(new BumpInTheNight()));
        harness.setGraveyard(player2, List.of(new BrimstoneVolley()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bump in the Night");
        harness.assertNotInGraveyard(player1, "Bump in the Night");
        harness.assertInGraveyard(player2, "Brimstone Volley");
    }

    @Test
    @DisplayName("Upkeep can return an instant cast in response to the trigger")
    void upkeepSelectsCardAtResolution() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Brimstone Volley");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Brimstone Volley");
        harness.assertNotInGraveyard(player1, "Brimstone Volley");
    }

    @Test
    @DisplayName("Casting a sorcery gives a boost that expires at end of turn")
    void sorceryBoostExpiresAtEndOfTurn() {
        Permanent devils = addCreatureReady(player1, new CharmbreakerDevils());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BumpInTheNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(devils.getPowerModifier()).isEqualTo(4);
        assertThat(devils.getToughnessModifier()).isZero();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(devils.getPowerModifier()).isZero();
        assertThat(devils.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's instant does not boost the Devils")
    void opponentsInstantDoesNotBoostDevils() {
        Permanent devils = addCreatureReady(player1, new CharmbreakerDevils());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new BrimstoneVolley()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(devils.getPowerModifier()).isZero();
        assertThat(devils.getToughnessModifier()).isZero();
    }

    @Test
    @CardUsed({CharmbreakerDevils.class, BumpInTheNight.class, RakshasaVizier.class})
    @DisplayName("Returning a card to hand does not trigger exile-from-graveyard abilities")
    void returningCardDoesNotCountAsExilingIt() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());
        Permanent vizier = harness.addToBattlefieldAndReturn(player1, new RakshasaVizier());
        harness.setGraveyard(player1, List.of(new BumpInTheNight()));

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        harness.assertInHand(player1, "Bump in the Night");
        harness.assertNotInGraveyard(player1, "Bump in the Night");
        assertThat(gd.stack).isEmpty();
        assertThat(vizier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
