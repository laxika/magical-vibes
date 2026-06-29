package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeismicAssaultTest extends BaseCardTest {


    @Test
    @DisplayName("Seismic Assault has correct card properties")
    void hasCorrectProperties() {
        SeismicAssault card = new SeismicAssault();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(2);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(0)).isInstanceOf(DiscardCardTypeCost.class);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(1)).isInstanceOf(DealDamageToAnyTargetEffect.class);
    }

    @Test
    @DisplayName("Activating ability starts discard-cost choice before stack entry")
    void activationStartsDiscardChoice() {
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.ACTIVATED_ABILITY_DISCARD_COST_CHOICE);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Choosing a land pays cost and puts ability on stack")
    void choosingLandPaysCostAndStacksAbility() {
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain(), new Plains()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 2);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getName().equals("Plains"));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Plains"));
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Seismic Assault");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot activate without a land card in hand")
    void cannotActivateWithoutLandCard() {
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a land card");
    }

    @Test
    @DisplayName("Cannot choose nonland for discard cost")
    void cannotChooseNonLandForDiscardCost() {
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));

        harness.activateAbility(player1, 0, null, player2.getId());

        // Choosing an invalid index re-prompts instead of throwing
        harness.handleCardChosen(player1, 0);

        // State should still be awaiting discard cost choice (re-prompted)
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.ACTIVATED_ABILITY_DISCARD_COST_CHOICE);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Illegal target is rejected before discard cost is paid")
    void illegalTargetRejectedBeforePayingCost() {
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new Mountain()));
        UUID illegalTarget = UUID.randomUUID();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, illegalTarget))
                .isInstanceOf(IllegalStateException.class);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deals 2 damage to target player")
    void deals2DamageToPlayer() {
        harness.setLife(player2, 20);
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new Mountain()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature")
    void deals2DamageToCreature() {
        addReadySeismicAssault(player1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID elfId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 0, null, elfId);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    private Permanent addReadySeismicAssault(Player player) {
        SeismicAssault card = new SeismicAssault();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}


