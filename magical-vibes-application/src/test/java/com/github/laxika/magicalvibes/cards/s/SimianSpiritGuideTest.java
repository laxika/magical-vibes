package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SimianSpiritGuide.class, RagingGoblin.class})
class SimianSpiritGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling from hand adds {R} to the pool")
    void exilingFromHandAddsRedMana() {
        harness.setHand(player1, List.of(new SimianSpiritGuide()));

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("The card is exiled, not discarded, and the ability never uses the stack")
    void cardIsExiledAndAbilityDoesNotUseTheStack() {
        harness.setHand(player1, List.of(new SimianSpiritGuide()));

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Simian Spirit Guide");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The added mana can pay for a spell")
    void addedManaPaysForASpell() {
        harness.setHand(player1, List.of(new SimianSpiritGuide(), new RagingGoblin()));

        harness.activateHandAbility(player1, 0, null);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Raging Goblin"));
    }
}
