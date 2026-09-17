package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Oblation.class, Mountain.class, BarkhideMauler.class})
class OblationTest extends BaseCardTest {

    @Test
    void shufflesTargetIntoItsOwnersLibraryThenThatPlayerDrawsTwo() {
        harness.setHand(player1, List.of(new Oblation()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain(), new Mountain()));
        harness.addToBattlefield(player2, new BarkhideMauler());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Barkhide Mauler");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    void drawsForTargetOwnerRatherThanController() {
        harness.setHand(player1, List.of(new Oblation()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain(), new Mountain()));

        BarkhideMauler target = new BarkhideMauler();
        target.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, target);
        UUID targetId = harness.getPermanentId(player2, "Barkhide Mauler");
        gd.stolenCreatures.put(targetId, player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    void cannotTargetLand() {
        harness.setHand(player1, List.of(new Oblation()));
        harness.addToBattlefield(player2, new Mountain());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
