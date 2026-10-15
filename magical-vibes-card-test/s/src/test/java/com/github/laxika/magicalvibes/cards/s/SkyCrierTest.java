package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyCrier.class})
class SkyCrierTest extends BaseCardTest {

    @Test
    @DisplayName("Sky Crier's ability makes you and target opponent draw a card without tapping")
    void eachPlayerDraws() {
        Permanent skyCrier = addCreatureReady(player1, new SkyCrier());
        addActivationMana(player1);
        prepareDraws();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(skyCrier.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Sky Crier");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Sky Crier");
    }

    @Test
    @DisplayName("Sky Crier's ability cannot target its controller")
    void abilityCannotTargetController() {
        addCreatureReady(player1, new SkyCrier());
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Sky Crier's ability cannot be activated without mana")
    void requiresMana() {
        Permanent skyCrier = addCreatureReady(player1, new SkyCrier());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(gd.stack).isEmpty();
        assertThat(skyCrier.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sky Crier's ability requires white mana")
    void requiresWhiteMana() {
        addCreatureReady(player1, new SkyCrier());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Sky Crier's ability requires three generic mana as well as white")
    void requiresFullGenericCost() {
        addCreatureReady(player1, new SkyCrier());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Sky Crier's ability consumes its mana payment")
    void spendsManaOnActivation() {
        addCreatureReady(player1, new SkyCrier());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Sky Crier can activate its ability while tapped")
    void canActivateWhileTapped() {
        Permanent skyCrier = addCreatureReady(player1, new SkyCrier());
        skyCrier.tap();
        addActivationMana(player1);
        prepareDraws();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(skyCrier.isTapped()).isTrue();
        harness.assertInHand(player1, "Sky Crier");
        harness.assertInHand(player2, "Sky Crier");
    }

    @Test
    @DisplayName("Sky Crier can activate its ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        Permanent skyCrier = harness.addToBattlefieldAndReturn(player1, new SkyCrier());
        skyCrier.setSummoningSick(true);
        addActivationMana(player1);
        prepareDraws();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(skyCrier.isTapped()).isFalse();
        harness.assertInHand(player1, "Sky Crier");
        harness.assertInHand(player2, "Sky Crier");
    }

    @Test
    @DisplayName("Sky Crier can activate twice by paying for each activation")
    void canActivateRepeatedly() {
        Permanent skyCrier = addCreatureReady(player1, new SkyCrier());
        addActivationMana(player1);
        addActivationMana(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new SkyCrier(), new SkyCrier()));
        harness.setLibrary(player2, List.of(new SkyCrier(), new SkyCrier()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(skyCrier.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The active opponent draws first when Sky Crier resolves during their turn")
    void activeOpponentDrawsFirst() {
        addCreatureReady(player1, new SkyCrier());
        addActivationMana(player1);
        prepareDraws();
        harness.forceActivePlayer(player2);
        gd.gameLog.clear();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(text -> text.endsWith(" draws a card.")).toList())
                .containsExactly(gd.playerIdToName.get(player2.getId()) + " draws a card.",
                        gd.playerIdToName.get(player1.getId()) + " draws a card.");
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    private void prepareDraws() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new SkyCrier()));
        harness.setLibrary(player2, List.of(new SkyCrier()));
    }
}
