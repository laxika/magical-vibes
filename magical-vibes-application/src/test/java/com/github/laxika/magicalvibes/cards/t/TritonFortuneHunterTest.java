package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TritonFortuneHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Triton Fortune Hunter draws a card")
    void castingSpellThatTargetsHunterDrawsACard() {
        harness.addToBattlefield(player1, new TritonFortuneHunter());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID hunterId = harness.getPermanentId(player1, "Triton Fortune Hunter");
        harness.castInstant(player1, 0, hunterId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Triton Fortune Hunter")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new TritonFortuneHunter());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's spell that targets Triton Fortune Hunter does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new TritonFortuneHunter());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID hunterId = harness.getPermanentId(player1, "Triton Fortune Hunter");
        harness.castInstant(player2, 0, hunterId);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
