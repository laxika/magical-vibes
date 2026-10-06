package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CodexShredder;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.SellerOfSongbirds;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RestInPeace.class, GrizzlyBears.class, Island.class, Shock.class, Naturalize.class,
        SellerOfSongbirds.class, CodexShredder.class})
class RestInPeaceTest extends BaseCardTest {

    private void castRestInPeace() {
        harness.castFromHand(player1, new RestInPeace(), "{1}{W}");
        harness.passBothPriorities();
        // Resolve the enters-the-battlefield trigger that went on the stack.
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Entering exiles every player's graveyard")
    void entersAndExilesAllGraveyards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Island()));

        castRestInPeace();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Island"));
    }

    @Test
    @DisplayName("The controller's dying creature is exiled instead of going to their graveyard")
    void ownDyingCreatureIsExiled() {
        castRestInPeace();
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("An opponent's dying creature is exiled instead of going to their graveyard")
    void opponentDyingCreatureIsExiled() {
        castRestInPeace();
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A resolved spell is exiled instead of being put into its owner's graveyard")
    void resolvedSpellIsExiled() {
        castRestInPeace();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    void destroyedRestInPeaceExilesItselfButNotTheDestroyingSpell() {
        castRestInPeace();
        UUID targetId = harness.getPermanentId(player1, "Rest in Peace");
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Rest in Peace");
        harness.assertNotInGraveyard(player1, "Rest in Peace");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rest in Peace"));
        harness.assertInGraveyard(player1, "Naturalize");
    }

    @Test
    void destroyedRestInPeaceOwnedByOpponentStillExilesItself() {
        RestInPeace restInPeace = new RestInPeace();
        restInPeace.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, restInPeace);
        UUID targetId = harness.getPermanentId(player1, "Rest in Peace");
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotInGraveyard(player2, "Rest in Peace");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(restInPeace);
        harness.assertInGraveyard(player1, "Naturalize");
    }

    @Test
    void controllersTokenDoesNotDie() {
        castRestInPeace();
        harness.castFromHand(player1, new SellerOfSongbirds(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        UUID birdId = harness.getPermanentId(player1, "Bird");
        int deathsBefore = gd.creatureDeathCountThisTurn.getOrDefault(player1.getId(), 0);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, birdId);

        harness.assertNotOnBattlefield(player1, "Bird");
        harness.assertNotInGraveyard(player1, "Bird");
        assertThat(gd.creatureDeathCountThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(deathsBefore);
    }

    @Test
    void millingEitherPlayerExilesTheCard() {
        castRestInPeace();
        for (var player : List.of(player1, player2)) {
            Island island = new Island();
            harness.setLibrary(player, List.of(island));
            harness.addToBattlefield(player1, new CodexShredder());
            int shredderIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;

            harness.activateAbility(player1, shredderIndex, 0, null, player.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player.getId())).isEmpty();
            assertThat(gd.getPlayerExiledCards(player.getId())).contains(island);
        }
    }
}
