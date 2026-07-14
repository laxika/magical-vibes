package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutrageShamanTest extends BaseCardTest {

    private Permanent targetOf(UUID id) {
        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("ETB damage equals red symbols in own cost when it is the only permanent (self counts)")
    void etbDamageFromOwnRedSymbols() {
        // Outrage Shaman {3}{R}{R} = 2 red symbols on the battlefield when the trigger resolves.
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(5);
        harness.addToBattlefield(player2, bear);
        harness.setHand(player1, List.of(new OutrageShaman()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities(); // Resolve creature → ETB triggers
        harness.passBothPriorities(); // Resolve ETB

        assertThat(gd.stack).isEmpty();
        assertThat(targetOf(targetId).getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Red symbols across all controlled permanents add up (Hill Giant + self = 3)")
    void etbCountsRedSymbolsAcrossPermanents() {
        // Hill Giant {3}{R} = 1 red symbol; Outrage Shaman {3}{R}{R} = 2. Total = 3.
        harness.addToBattlefield(player1, new HillGiant());
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(5);
        harness.addToBattlefield(player2, bear);
        harness.setHand(player1, List.of(new OutrageShaman()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(targetOf(targetId).getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-red permanents contribute no symbols")
    void etbIgnoresNonRedSymbols() {
        // Grizzly Bears {1}{G} on your side = 0 red symbols; only the Shaman's own {R}{R} counts.
        harness.addToBattlefield(player1, new GrizzlyBears());
        GrizzlyBears target = new GrizzlyBears();
        target.setToughness(5);
        harness.addToBattlefield(player2, target);
        harness.setHand(player1, List.of(new OutrageShaman()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(targetOf(targetId).getMarkedDamage()).isEqualTo(2);
    }
}
