package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreechingSkaab.class, Forest.class})
class ScreechingSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Screeching Skaab puts it on stack as creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ScreechingSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Screeching Skaab");
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.setHand(player1, List.of(new ScreechingSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Screeching Skaab");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Screeching Skaab");
    }

    @Test
    @DisplayName("ETB ability mills controller's top 2 cards into graveyard")
    void etbMillsTwoCards() {
        Forest f1 = new Forest();
        Forest f2 = new Forest();

        harness.setHand(player1, List.of(new ScreechingSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player1, List.of(f1, f2));

        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).size()).isEqualTo(graveyardBefore + 2);
    }

    @Test
    @DisplayName("ETB mills controller, not opponent")
    void etbMillsControllerNotOpponent() {
        harness.setHand(player1, List.of(new ScreechingSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();
        int opponentGraveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(opponentDeckBefore);
        assertThat(gd.playerGraveyards.get(player2.getId()).size()).isEqualTo(opponentGraveyardBefore);
    }

    @Test
    @DisplayName("ETB mills fewer cards if library has less than 2")
    void etbMillsFewerIfLibrarySmall() {
        Forest f1 = new Forest();

        harness.setHand(player1, List.of(new ScreechingSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player1, List.of(f1));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("ETB mills only the top two cards of a larger library")
    void etbMillsOnlyTopTwoCards() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new ScreechingSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        harness.assertOnBattlefield(player1, "Screeching Skaab");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB resolves normally with an empty library")
    void etbWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new ScreechingSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Screeching Skaab");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }
}
