package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RummagingGoblinTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has one activated ability with tap, no mana cost, discard cost then draw")
    void hasCorrectAbility() {
        RummagingGoblin card = new RummagingGoblin();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(DiscardCardTypeCost.class);
        DiscardCardTypeCost discardCost = (DiscardCardTypeCost) ability.getEffects().get(0);
        assertThat(discardCost.predicate()).isNull();
        assertThat(ability.getEffects().get(1)).isInstanceOf(DrawCardEffect.class);
    }

    // ===== Activated ability — discard cost =====

    @Test
    @DisplayName("Activating ability starts discard-cost choice for any card")
    void activationStartsDiscardChoice() {
        addReadyGoblin(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.ACTIVATED_ABILITY_DISCARD_COST_CHOICE);
        assertThat(gd.stack).isEmpty();
        // All cards should be valid since predicate is null (any card)
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(0, 1);
    }

    @Test
    @DisplayName("Choosing a card pays cost and puts ability on stack")
    void choosingCardPaysCostAndStacksAbility() {
        addReadyGoblin(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Rummaging Goblin");
    }

    @Test
    @DisplayName("Cannot activate without cards in hand")
    void cannotActivateWithoutCardsInHand() {
        addReadyGoblin(player1);
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Activated ability — resolution =====

    @Test
    @DisplayName("Resolving ability draws a card")
    void resolvingDrawsACard() {
        addReadyGoblin(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        // Hand had 1 card, discarded 1 as cost (hand = 0), then drew 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("draws a card"));
    }

    @Test
    @DisplayName("Can discard any card type as cost — lands work too")
    void canDiscardLandAsCost() {
        addReadyGoblin(player1);
        harness.setHand(player1, List.of(new Mountain()));
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Mountain"));
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Tap restrictions =====

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent goblin = addReadyGoblin(player1);
        goblin.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        RummagingGoblin card = new RummagingGoblin();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating ability taps Rummaging Goblin after paying discard cost")
    void activatingTapsGoblin() {
        Permanent goblin = addReadyGoblin(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(goblin.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyGoblin(Player player) {
        RummagingGoblin card = new RummagingGoblin();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
