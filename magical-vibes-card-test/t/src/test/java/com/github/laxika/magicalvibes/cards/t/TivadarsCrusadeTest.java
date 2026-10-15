package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TivadarsCrusade.class, GoblinHero.class, Squire.class})
class TivadarsCrusadeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys Goblins on both battlefields and spares non-Goblins")
    void destroysAllGoblins() {
        harness.addToBattlefield(player1, new GoblinHero());
        harness.addToBattlefield(player2, new GoblinHero());
        harness.addToBattlefield(player2, new Squire());

        harness.castFromHand(player1, new TivadarsCrusade(), "{1}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Hero");
        harness.assertNotOnBattlefield(player2, "Goblin Hero");
        harness.assertInGraveyard(player1, "Goblin Hero");
        harness.assertInGraveyard(player2, "Goblin Hero");
        harness.assertOnBattlefield(player2, "Squire");
        harness.assertNotInGraveyard(player2, "Squire");
        harness.assertInGraveyard(player1, "Tivadar's Crusade");
    }

    @Test
    @DisplayName("Resolves without any Goblins on the battlefield")
    void resolvesWithoutGoblins() {
        harness.addToBattlefield(player1, new Squire());

        harness.castFromHand(player1, new TivadarsCrusade(), "{1}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Squire");
        harness.assertNotInGraveyard(player1, "Squire");
        harness.assertInGraveyard(player1, "Tivadar's Crusade");
    }

    @Test
    @DisplayName("Goblins can regenerate from the destruction")
    void regenerationSavesGoblin() {
        Permanent protectedGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinHero());
        protectedGoblin.setRegenerationShield(1);
        harness.addToBattlefield(player2, new GoblinHero());

        harness.castFromHand(player1, new TivadarsCrusade(), "{1}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Hero");
        harness.assertNotInGraveyard(player1, "Goblin Hero");
        assertThat(protectedGoblin.isTapped()).isTrue();
        assertThat(protectedGoblin.getRegenerationShield()).isZero();
        harness.assertNotOnBattlefield(player2, "Goblin Hero");
        harness.assertInGraveyard(player2, "Goblin Hero");
    }

    @Test
    @DisplayName("Goblins in hand and library are unaffected")
    void doesNotDestroyGoblinCardsOutsideBattlefield() {
        GoblinHero libraryGoblin = new GoblinHero();
        harness.setHand(player2, List.of(new GoblinHero()));
        harness.setLibrary(player2, List.of(libraryGoblin));
        harness.addToBattlefield(player2, new GoblinHero());

        harness.castFromHand(player1, new TivadarsCrusade(), "{1}{W}{W}");
        harness.passBothPriorities();

        harness.assertInHand(player2, "Goblin Hero");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryGoblin);
        harness.assertNotOnBattlefield(player2, "Goblin Hero");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Goblin Hero");
    }
}
