package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.d.DreadReturn;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiverKelpie.class, ReassemblingSkeleton.class, DreadReturn.class, AncientGrudge.class,
        PrismaticLens.class})
class RiverKelpieTest extends BaseCardTest {

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
