package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JaceBelerenTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeLoyaltyAbilities() {
        JaceBeleren card = new JaceBeleren();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+2 ability has EachPlayerDrawsCardEffect(1)")
    void plusTwoAbilityHasCorrectEffect() {
        JaceBeleren card = new JaceBeleren();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(2);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(EachPlayerDrawsCardEffect.class);
        assertThat(((EachPlayerDrawsCardEffect) ability.getEffects().getFirst()).amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("-1 ability has DrawCardForTargetPlayerEffect(1) targeting player")
    void minusOneAbilityHasCorrectEffect() {
        JaceBeleren card = new JaceBeleren();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-1);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(DrawCardForTargetPlayerEffect.class);
        DrawCardForTargetPlayerEffect effect = (DrawCardForTargetPlayerEffect) ability.getEffects().getFirst();
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.targetsPlayer()).isTrue();
    }

    @Test
    @DisplayName("-10 ability has MillTargetPlayerEffect(20)")
    void minusTenAbilityHasCorrectEffect() {
        JaceBeleren card = new JaceBeleren();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-10);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(MillTargetPlayerEffect.class);
        assertThat(((MillTargetPlayerEffect) ability.getEffects().getFirst()).count()).isEqualTo(20);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new JaceBeleren()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Jace Beleren");
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty 3")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new JaceBeleren()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Jace Beleren"));
        Permanent jace = bf.stream().filter(p -> p.getCard().getName().equals("Jace Beleren")).findFirst().orElseThrow();
        assertThat(jace.getLoyaltyCounters()).isEqualTo(3);
        assertThat(jace.isSummoningSick()).isFalse();
    }

    // ===== +2 ability: Each player draws a card =====

    @Test
    @DisplayName("+2 ability makes each player draw a card and increases loyalty")
    void plusTwoEachPlayerDrawsCard() {
        Permanent jace = addReadyJace(player1);

        int p1HandBefore = harness.getGameData().playerHands.get(player1.getId()).size();
        int p2HandBefore = harness.getGameData().playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getLoyaltyCounters()).isEqualTo(5); // 3 + 2
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 1);
    }

    // ===== -1 ability: Target player draws a card =====

    @Test
    @DisplayName("-1 ability makes target player draw a card and decreases loyalty")
    void minusOneTargetPlayerDrawsCard() {
        Permanent jace = addReadyJace(player1);

        int p2HandBefore = harness.getGameData().playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getLoyaltyCounters()).isEqualTo(2); // 3 - 1
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 1);
    }

    @Test
    @DisplayName("-1 ability can target yourself")
    void minusOneCanTargetSelf() {
        Permanent jace = addReadyJace(player1);

        int p1HandBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getLoyaltyCounters()).isEqualTo(2); // 3 - 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
    }

    // ===== -10 ability: Target player mills twenty cards =====

    @Test
    @DisplayName("-10 ability mills twenty cards from target player's library")
    void minusTenMillsTwentyCards() {
        Permanent jace = addReadyJace(player1);
        jace.setLoyaltyCounters(10);

        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        while (deck.size() > 25) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();
        int graveyardBefore = harness.getGameData().playerGraveyards.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(jace.getLoyaltyCounters()).isEqualTo(0); // 10 - 10
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 20);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardBefore + 20);
    }

    @Test
    @DisplayName("-10 ability: Jace goes to graveyard at 0 loyalty")
    void minusTenJaceGoesToGraveyardAtZeroLoyalty() {
        Permanent jace = addReadyJace(player1);
        jace.setLoyaltyCounters(10);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Jace Beleren"));
    }

    @Test
    @DisplayName("Cannot use -10 when loyalty is only 3")
    void cannotActivateMinusTenWithInsufficientLoyalty() {
        addReadyJace(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Loyalty ability restrictions =====

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyJace(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyJace(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    // ===== Helpers =====

    private Permanent addReadyJace(Player player) {
        JaceBeleren card = new JaceBeleren();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(3);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
