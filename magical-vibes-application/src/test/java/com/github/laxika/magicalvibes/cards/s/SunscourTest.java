package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KjeldoranOutrider;
import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.cards.w.WhiteShieldCrusader;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sunscour.class, KjeldoranOutrider.class, WhiteShieldCrusader.class, MishrasBauble.class})
class SunscourTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and leaves noncreature permanents alone")
    void destroysAllCreatures() {
        harness.addToBattlefield(player1, new KjeldoranOutrider());
        harness.addToBattlefield(player2, new WhiteShieldCrusader());
        harness.addToBattlefield(player1, new MishrasBauble());
        harness.setHand(player1, List.of(new Sunscour()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kjeldoran Outrider");
        harness.assertNotOnBattlefield(player2, "White Shield Crusader");
        harness.assertOnBattlefield(player1, "Mishra's Bauble");
    }

    @Test
    @DisplayName("Can be cast by exiling two white cards from hand")
    void castsByExilingTwoWhiteCards() {
        harness.addToBattlefield(player2, new KjeldoranOutrider());
        harness.setHand(player1, List.of(new Sunscour(), new KjeldoranOutrider(), new WhiteShieldCrusader()));
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, null, false, 1, List.of(1, 2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kjeldoran Outrider");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactlyInAnyOrder("Kjeldoran Outrider", "White Shield Crusader");
    }

    @Test
    @DisplayName("Alternate cost requires exactly two matching hand cards")
    void alternateCostRequiresTwoWhiteCards() {
        harness.setHand(player1, List.of(new Sunscour(), new KjeldoranOutrider()));
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, null, false, 1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost rejects a non-white hand card")
    void alternateCostRequiresWhiteCards() {
        harness.setHand(player1, List.of(new Sunscour(), new KjeldoranOutrider(), new MishrasBauble()));
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, null, false, 1, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }
}
