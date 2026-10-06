package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.b.BurnTrail;
import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.d.DreadReturn;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.cards.s.Snakeform;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiverKelpie.class, ReassemblingSkeleton.class, DreadReturn.class, AncientGrudge.class,
        PrismaticLens.class, BurnTrail.class, Snakeform.class, BeaconOfUnrest.class})
class RiverKelpieTest extends BaseCardTest {

    @Test
    void drawsForNoncreaturePermanentReturningFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new RiverKelpie());
        PrismaticLens artifact = new PrismaticLens();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 0, artifact.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Prismatic Lens");
        harness.assertNotInGraveyard(player2, "Prismatic Lens");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void graveyardCastAndPermanentReturnEachDrawSeparately() {
        harness.addToBattlefield(player1, new RiverKelpie());
        var firstSacrifice = addCreatureReady(player1, new ReassemblingSkeleton());
        var secondSacrifice = addCreatureReady(player1, new ReassemblingSkeleton());
        var thirdSacrifice = addCreatureReady(player1, new ReassemblingSkeleton());
        ReassemblingSkeleton target = new ReassemblingSkeleton();
        harness.setGraveyard(player1, List.of(new DreadReturn(), target));
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromGraveyardWithSacrifices(player1, 0, target.getId(),
                List.of(firstSacrifice.getId(), secondSacrifice.getId(), thirdSacrifice.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Reassembling Skeleton");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    @Test
    void persistReturnsWithCounterAndDrawsButDoesNotReturnAfterSecondDeath() {
        harness.addToBattlefield(player1, new RiverKelpie());
        harness.setHand(player1, List.of(new BurnTrail()));
        harness.addMana(player1, ManaColor.RED, 4);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, harness.getPermanentId(player1, "River Kelpie"));
        resolveAllTriggers();

        var returned = findPermanent(player1, "River Kelpie");
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);

        harness.setHand(player1, List.of(new BurnTrail()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, returned.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "River Kelpie");
        harness.assertInGraveyard(player1, "River Kelpie");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    void doesNotDrawForGraveyardEntryAfterLosingAbilities() {
        harness.addToBattlefield(player1, new RiverKelpie());
        harness.setHand(player1, List.of(new Snakeform()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "River Kelpie"));
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Reassembling Skeleton");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    void drawsForOpponentPermanentReturningFromGraveyard() {
        harness.addToBattlefield(player1, new RiverKelpie());
        harness.setGraveyard(player2, List.of(new ReassemblingSkeleton()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateGraveyardAbility(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Reassembling Skeleton");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
    }

    @Test
    void doesNotDrawForPermanentEnteringFromHand() {
        harness.addToBattlefield(player1, new RiverKelpie());
        harness.setHand(player1, List.of(new PrismaticLens()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Prismatic Lens");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Drawing a card when a permanent enters from a graveyard")
    void drawsWhenPermanentEntersFromGraveyard() {
        harness.addToBattlefield(player1, new RiverKelpie());
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        // Reassembling Skeleton returns itself from the graveyard to the battlefield.
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Reassembling Skeleton");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Drawing a card when a spell is cast from a graveyard")
    void drawsWhenSpellCastFromGraveyard() {
        harness.addToBattlefield(player1, new RiverKelpie());
        harness.addToBattlefield(player2, new PrismaticLens());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        UUID targetId = harness.getPermanentId(player2, "Prismatic Lens");
        harness.castFlashback(player1, 0, targetId);
        resolveAllTriggers();

        // The cast-from-graveyard trigger drew a card (net +1, independent of the spell's effect).
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Drawing a card when River Kelpie returns from a graveyard")
    void drawsWhenThisCreatureEntersFromGraveyard() {
        var firstSacrifice = addCreatureReady(player1, new ReassemblingSkeleton());
        var secondSacrifice = addCreatureReady(player1, new ReassemblingSkeleton());
        var thirdSacrifice = addCreatureReady(player1, new ReassemblingSkeleton());
        RiverKelpie kelpie = new RiverKelpie();
        DreadReturn spell = new DreadReturn();
        harness.setGraveyard(player1, List.of(spell, kelpie));

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromGraveyardWithSacrifices(player1, 0, kelpie.getId(),
                List.of(firstSacrifice.getId(), secondSacrifice.getId(), thirdSacrifice.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "River Kelpie");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("The controller draws when an opponent casts a spell from a graveyard")
    void controllerDrawsWhenOpponentCastsSpellFromGraveyard() {
        harness.addToBattlefield(player1, new RiverKelpie());
        harness.addToBattlefield(player1, new PrismaticLens());
        harness.setGraveyard(player2, List.of(new AncientGrudge()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        UUID targetId = harness.getPermanentId(player1, "Prismatic Lens");
        harness.castFlashback(player2, 0, targetId);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
    }

    @Test
    @DisplayName("No draw when a spell is cast from hand")
    void noDrawWhenSpellCastFromHand() {
        harness.addToBattlefield(player1, new RiverKelpie());
        harness.addToBattlefield(player2, new PrismaticLens());
        harness.setHand(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.RED, 2);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        UUID targetId = harness.getPermanentId(player2, "Prismatic Lens");
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();

        // Casting from hand does not trigger the graveyard-cast draw — the library is untouched.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }
}
