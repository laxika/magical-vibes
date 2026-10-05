package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemorialToGenius.class})
class MemorialToGeniusTest extends BaseCardTest {


    @Test
    @DisplayName("Memorial to Genius enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new MemorialToGenius()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent memorial = findPermanent(player1, "Memorial to Genius");
        assertThat(memorial.isTapped()).isTrue();
    }


    @Test
    @DisplayName("Tapping Memorial to Genius produces blue mana")
    void tappingProducesBlueMana() {
        Permanent memorial = addMemorialReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(memorial);

        harness.tapPermanent(player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }


    @Test
    @DisplayName("Activating sacrifice ability puts it on the stack")
    void sacrificeAbilityPutsOnStack() {
        addMemorialReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Memorial to Genius");
    }

    @Test
    @DisplayName("Memorial is sacrificed as a cost before resolution")
    void sacrificedBeforeResolution() {
        addMemorialReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Memorial to Genius");
        harness.assertInGraveyard(player1, "Memorial to Genius");
    }

    @Test
    @DisplayName("Resolving sacrifice ability draws two cards")
    void resolvingSacrificeAbilityDrawsTwoCards() {
        addMemorialReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Mana is consumed when activating sacrifice ability")
    void manaIsConsumedWhenActivating() {
        addMemorialReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent memorial = addMemorialReady(player1);
        memorial.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot pay the draw ability's blue cost with only colorless mana")
    void cannotActivateWithoutBlueMana() {
        Permanent memorial = addMemorialReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(memorial.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Memorial to Genius");
        harness.assertNotInGraveyard(player1, "Memorial to Genius");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate the draw ability with only four mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent memorial = addMemorialReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(memorial.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Memorial to Genius");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Draw ability can be activated during the opponent's turn and draws only on resolution")
    void activatesDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new MemorialToGenius());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MemorialToGenius(), new MemorialToGenius()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Memorial to Genius");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addMemorialReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MemorialToGenius());
        perm.setSummoningSick(false);
        return perm;
    }
}
