package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ElvishSpiritGuide.class)
class ElvishSpiritGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling from hand adds {G} to the pool")
    void exilingFromHandAddsGreenMana() {
        harness.setHand(player1, List.of(new ElvishSpiritGuide()));

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The card is exiled, not discarded, and the ability never uses the stack")
    void cardIsExiledAndAbilityDoesNotUseTheStack() {
        harness.setHand(player1, List.of(new ElvishSpiritGuide()));

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Elvish Spirit Guide");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The added mana can pay for a spell")
    void addedManaPaysForASpell() {
        harness.setHand(player1, List.of(new ElvishSpiritGuide(), new ElvishSpiritGuide()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elvish Spirit Guide");
    }
}
