package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FirespoutTest extends BaseCardTest {

    @Test
    @DisplayName("Only {R} spent: kills non-flyers, spares flyers")
    void redOnlyKillsNonFlyers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        harness.setHand(player1, List.of(new Firespout()));
        // {2} generic + {R/G} hybrid all paid with red → only {R} spent
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
    }

    @Test
    @DisplayName("Only {G} spent: kills flyers, spares non-flyers")
    void greenOnlyKillsFlyers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        harness.setHand(player1, List.of(new Firespout()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
    }

    @Test
    @DisplayName("{R}{G} spent: kills both flyers and non-flyers")
    void bothColorsKillEverything() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        harness.setHand(player1, List.of(new Firespout()));
        // Pay with both colors so both {R} and {G} count as spent (generic {2} draws the
        // off-color the hybrid didn't take).
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
    }
}
