package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FirefrightMage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SimianSpiritGuide.class, FirefrightMage.class})
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
        harness.setHand(player1, List.of(new SimianSpiritGuide(), new FirefrightMage()));

        harness.activateHandAbility(player1, 0, null);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Firefright Mage");
    }

    @Test
    @DisplayName("Mana ability resolves immediately on an opponent's turn with a spell on the stack")
    void activatesOnOpponentsTurnWithSpellOnStack() {
        harness.setHand(player1, List.of(new FirefrightMage()));
        harness.setHand(player2, List.of(new SimianSpiritGuide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        var spell = gd.stack.getFirst();

        harness.activateHandAbility(player2, 0, null);

        assertThat(gd.stack).containsExactly(spell);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Simian Spirit Guide");
        harness.assertNotOnBattlefield(player1, "Firefright Mage");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Firefright Mage");
    }
}
