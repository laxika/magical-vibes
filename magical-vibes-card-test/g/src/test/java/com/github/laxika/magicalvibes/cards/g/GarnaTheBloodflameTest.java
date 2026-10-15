package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DreamTwist;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarnaTheBloodflame.class, GrizzlyBears.class, Shock.class, DreamTwist.class})
class GarnaTheBloodflameTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns creature that died this turn from the battlefield")
    void etbReturnsCreatureThatDiedThisTurn() {
        Card creature = new GrizzlyBears();

        harness.addToBattlefield(player1, creature);
        harness.addToBattlefield(player1, new GrizzlyBears()); // a second one to shock

        UUID targetCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetCreatureId);

        // The killed creature should be in the graveyard now
        Card diedCreature = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Cast Garna
        harness.castFromHand(player1, new GarnaTheBloodflame(), "{3}{B}{R}");
        harness.passBothPriorities(); // resolve creature, ETB triggers
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(diedCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(diedCreature.getId()));
    }

    @Test
    @DisplayName("ETB returns creature card that was milled this turn (from anywhere, not just battlefield)")
    void etbReturnsMilledCreatureCard() {
        // Put a creature card on top of the library so it gets milled
        Card creatureInLibrary = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, creatureInLibrary);

        // Self-mill with Dream Twist
        harness.setHand(player1, List.of(new DreamTwist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        // Verify the creature was milled into graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureInLibrary.getId()));

        // Cast Garna
        harness.castFromHand(player1, new GarnaTheBloodflame(), "{3}{B}{R}");
        harness.passBothPriorities(); // resolve creature, ETB triggers
        harness.passBothPriorities(); // resolve ETB

        // The milled creature card should be returned to hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureInLibrary.getId()));
    }

    @Test
    @DisplayName("ETB does not return creature cards already in graveyard before this turn")
    void etbDoesNotReturnCreaturesFromPreviousTurn() {
        Card oldCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldCreature));

        // Cast Garna
        harness.castFromHand(player1, new GarnaTheBloodflame(), "{3}{B}{R}");
        harness.passBothPriorities(); // resolve creature, ETB triggers
        harness.passBothPriorities(); // resolve ETB

        // Old creature should still be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(oldCreature.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(oldCreature.getId()));
    }

    @Test
    @DisplayName("ETB does not return non-creature cards put into graveyard this turn")
    void etbDoesNotReturnNonCreatureCards() {
        // Put a non-creature card on top of the library so it gets milled
        Card nonCreature = new Shock();
        gd.playerDecks.get(player1.getId()).add(0, nonCreature);

        // Self-mill with Dream Twist to put the non-creature into graveyard
        harness.setHand(player1, List.of(new DreamTwist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        // Cast Garna
        harness.castFromHand(player1, new GarnaTheBloodflame(), "{3}{B}{R}");
        harness.passBothPriorities(); // resolve creature, ETB triggers
        harness.passBothPriorities(); // resolve ETB

        // Shock should remain in graveyard (not a creature card)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(nonCreature.getId()));
    }

    @Test
    @DisplayName("Does not return creature cards put into an opponent's graveyard this turn")
    void doesNotReturnOpponentsCreatures() {
        Card opponentsCreature = new GrizzlyBears();

        harness.addToBattlefield(player2, opponentsCreature);

        UUID targetCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetCreatureId);

        // Cast Garna
        harness.castFromHand(player1, new GarnaTheBloodflame(), "{3}{B}{R}");
        harness.passBothPriorities(); // resolve creature, ETB triggers
        harness.passBothPriorities(); // resolve ETB

        // Opponent's creature should still be in opponent's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(opponentsCreature.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(opponentsCreature.getId()));
    }

    @Test
    @DisplayName("Other creatures you control have haste while Garna is on the battlefield")
    void otherCreaturesHaveHaste() {
        harness.addToBattlefield(player1, new GarnaTheBloodflame());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Garna does not grant haste to itself (OWN_CREATURES scope)")
    void garnaDoesNotGrantHasteToItself() {
        harness.addToBattlefield(player1, new GarnaTheBloodflame());

        Permanent garna = findPermanent(player1, "Garna, the Bloodflame");

        assertThat(gqs.hasKeyword(gd, garna, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste is lost when Garna leaves the battlefield")
    void hasteLostWhenGarnaLeaves() {
        GarnaTheBloodflame garnaCard = new GarnaTheBloodflame();
        harness.addToBattlefield(player1, garnaCard);
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        // Remove Garna via Shock
        UUID garnaPermId = harness.getPermanentId(player1, "Garna, the Bloodflame");
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        // Garna is 3/3, needs two Shocks to kill
        harness.castAndResolveInstant(player1, 0, garnaPermId);
        harness.castAndResolveInstant(player1, 0, garnaPermId);

        // Bears should no longer have haste
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("ETB returns all creatures milled in response to the trigger")
    void returnsAllCreaturesMilledInResponse() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.castFromHand(player1, new GarnaTheBloodflame(), "{3}{B}{R}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DreamTwist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, third);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second, third);
        harness.assertInGraveyard(player1, "Dream Twist");
    }

    @Test
    @DisplayName("Garna returns itself if it dies before its ETB resolves")
    void returnsItselfWhenKilledInResponse() {
        GarnaTheBloodflame garna = new GarnaTheBloodflame();
        harness.castFromHand(player1, garna, "{3}{B}{R}");
        harness.passBothPriorities();
        UUID garnaId = harness.getPermanentId(player1, "Garna, the Bloodflame");

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, garnaId);
        harness.castAndResolveInstant(player1, 0, garnaId);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(garna);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(garna);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(garna);
        harness.assertNotOnBattlefield(player1, "Garna, the Bloodflame");
    }

    @Test
    @DisplayName("Garna does not grant haste to opposing creatures")
    void opponentsCreaturesDoNotHaveHaste() {
        harness.addToBattlefield(player1, new GarnaTheBloodflame());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Flash allows Garna to enter during an opponent's upkeep")
    void canBeCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromHand(player1, new GarnaTheBloodflame(), "{3}{B}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Garna, the Bloodflame");
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }
}
