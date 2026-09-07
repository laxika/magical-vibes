package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchonOfAbsolution.class, GrizzlyBears.class})
class ArchonOfAbsolutionTest extends BaseCardTest {

    @Test
    @DisplayName("Archon of Absolution has protection from white")
    void hasProtectionFromWhite() {
        Permanent archon = harness.addToBattlefieldAndReturn(player1, new ArchonOfAbsolution());

        assertThat(gqs.hasProtectionFrom(gd, archon, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, archon, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Opponent pays {1} for each creature attacking the controller")
    void opponentPaysOnePerAttacker() {
        harness.addToBattlefield(player1, new ArchonOfAbsolution());
        addReadyCreature(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(player2, List.of(0), null);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent cannot attack without paying the tax")
    void opponentCannotAttackWithoutPayment() {
        harness.addToBattlefield(player1, new ArchonOfAbsolution());
        addReadyCreature(player2);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("The tax also applies to attacks against the controller's planeswalker")
    void planeswalkerAttackIsTaxed() {
        harness.addToBattlefield(player1, new ArchonOfAbsolution());
        Permanent planeswalker = addPlaneswalker(player1);
        addReadyCreature(player2);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private void addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
    }

    private Permanent addPlaneswalker(Player player) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(4);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
