package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EmblemGrantsFlashbackEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

class JayaBallardTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeAbilities() {
        JayaBallard card = new JayaBallard();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    

    

    

    // ===== +1 mana ability =====

    @Test
    @DisplayName("+1 mana ability adds 3 restricted red mana")
    void plusOneManaAbilityAddsRestrictedMana() {
        Permanent jaya = addReadyJaya(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(3);
    }

    // ===== +1 rummage ability =====

    @Test
    @DisplayName("+1 rummage: discard 2, draw 2")
    void plusOneRummageDiscardTwoDraw() {
        Permanent jaya = addReadyJaya(player1);
        Card cardA = new GrizzlyBears();
        Card cardB = new GrizzlyBears();
        Card cardC = new LightningBolt();
        harness.setHand(player1, List.of(cardA, cardB, cardC));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Should be awaiting X value choice for how many to discard
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class) != null).isTrue();

        // Choose to discard 2
        harness.handleXValueChosen(player1, 2);

        // Should be awaiting discard choice
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null).isTrue();

        // Discard first card
        harness.handleCardChosen(player1, 0);

        // Should be awaiting second discard
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null).isTrue();

        // Discard second card
        harness.handleCardChosen(player1, 0);

        // Hand should have 1 original card + 2 drawn cards = 3 cards
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        // Graveyard should have 2 discarded cards
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        // Loyalty should be 5 + 1 = 6
        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("+1 rummage: choose 0, no discard or draw")
    void plusOneRummageChooseZero() {
        Permanent jaya = addReadyJaya(player1);
        Card cardA = new GrizzlyBears();
        harness.setHand(player1, List.of(cardA));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class) != null).isTrue();

        // Choose to discard 0
        harness.handleXValueChosen(player1, 0);

        // Hand should be unchanged (1 card)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Graveyard should be empty
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("+1 rummage: empty hand does nothing")
    void plusOneRummageEmptyHand() {
        Permanent jaya = addReadyJaya(player1);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Should NOT be awaiting any input — effect should resolve immediately
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== -8 emblem =====

    @Test
    @DisplayName("-8 creates emblem with EmblemGrantsFlashbackEffect")
    void minusEightCreatesEmblem() {
        Permanent jaya = addReadyJaya(player1);
        jaya.setCounterCount(CounterType.LOYALTY, 8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        assertThat(emblem.staticEffects()).hasSize(1);
        assertThat(emblem.staticEffects().getFirst()).isInstanceOf(EmblemGrantsFlashbackEffect.class);

        EmblemGrantsFlashbackEffect effect = (EmblemGrantsFlashbackEffect) emblem.staticEffects().getFirst();
        assertThat(effect.cardTypes()).containsExactlyInAnyOrder(CardType.INSTANT, CardType.SORCERY);
    }

    @Test
    @DisplayName("Cannot activate -8 with only 5 loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadyJaya(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Helpers =====

    private Permanent addReadyJaya(Player player) {
        JayaBallard card = new JayaBallard();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
