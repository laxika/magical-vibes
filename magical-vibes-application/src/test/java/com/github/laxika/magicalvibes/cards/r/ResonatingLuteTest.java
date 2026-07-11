package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResonatingLuteTest extends BaseCardTest {

    @Test
    @DisplayName("Grants lands a mana ability and has a hand-gated draw ability")
    void hasCorrectStructure() {
        ResonatingLute card = new ResonatingLute();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(GrantActivatedAbilityEffect.class::isInstance);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        ActivatedAbility draw = card.getActivatedAbilities().getFirst();
        assertThat(draw.getMinCardsInHandToActivate()).isEqualTo(7);
    }

    @Test
    @DisplayName("Draw ability cannot be activated with fewer than seven cards in hand")
    void drawAbilityRequiresSevenCards() {
        harness.addToBattlefield(player1, new ResonatingLute());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("7 or more cards in your hand");
    }

    @Test
    @DisplayName("Draw ability works with seven or more cards in hand")
    void drawAbilityWorksWithSevenCards() {
        harness.addToBattlefield(player1, new ResonatingLute());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }

    @Test
    @DisplayName("Lands you control gain a two-mana ability spendable only on instants and sorceries")
    void landsGainRestrictedManaAbility() {
        harness.addToBattlefield(player1, new ResonatingLute());
        harness.addToBattlefield(player1, new Island());

        // Island (battlefield index 1) gains the granted ability at ability index 0.
        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(2);
    }
}
