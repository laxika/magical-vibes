package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.cards.m.MoonragersSlash;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FamishedForagers.class, CandlegroveWitch.class, MoonragersSlash.class})
class FamishedForagersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without adding mana when no opponent lost life this turn")
    void noManaWithoutOpponentLifeLoss() {
        harness.setHand(player1, List.of(new FamishedForagers()));
        harness.addMana(player1, ManaColor.RED, 4);
        forceMainPhase(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Adds three red mana when an opponent lost life this turn")
    void addsManaAfterOpponentLifeLoss() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MoonragersSlash(), new FamishedForagers()));
        harness.addMana(player1, ManaColor.RED, 7);
        forceMainPhase(player1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        forceMainPhase(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Discards a card to draw a card")
    void discardsAndDraws() {
        var foragers = harness.addToBattlefieldAndReturn(player1, new FamishedForagers());
        foragers.setSummoningSick(false);
        harness.setHand(player1, List.of(new CandlegroveWitch()));
        harness.setLibrary(player1, List.of(new CandlegroveWitch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        forceMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Candlegrove Witch");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Candlegrove Witch");
    }

    @Test
    @DisplayName("Controller life loss does not satisfy the entry condition")
    void controllerLifeLossDoesNotAddMana() {
        harness.setHand(player1, List.of(new MoonragersSlash(), new FamishedForagers()));
        harness.addMana(player1, ManaColor.RED, 7);
        forceMainPhase(player1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        forceMainPhase(player1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Life loss after entry does not retroactively trigger the mana ability")
    void laterLifeLossDoesNotTrigger() {
        harness.setHand(player1, List.of(new FamishedForagers(), new MoonragersSlash()));
        harness.addMana(player1, ManaColor.RED, 7);
        forceMainPhase(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The entry trigger uses the stack and resolves after Foragers dies")
    void manaTriggerResolvesWithoutItsSource() {
        harness.setHand(player1, List.of(
                new MoonragersSlash(), new FamishedForagers(), new MoonragersSlash()));
        harness.addMana(player1, ManaColor.RED, 10);
        forceMainPhase(player1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        forceMainPhase(player1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Famished Foragers"));
        harness.assertInGraveyard(player1, "Famished Foragers");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("A summoning-sick Foragers can discard an instant and draws only on resolution")
    void summoningSickForagersCanDiscardInstant() {
        var foragers = harness.addToBattlefieldAndReturn(player1, new FamishedForagers());
        foragers.setSummoningSick(true);
        harness.setHand(player1, List.of(new MoonragersSlash()));
        harness.setLibrary(player1, List.of(new CandlegroveWitch()));
        harness.addMana(player1, ManaColor.RED, 3);
        forceMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Moonrager's Slash");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Candlegrove Witch");
    }

    @Test
    @DisplayName("The draw ability cannot be activated without a card to discard")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefield(player1, new FamishedForagers());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 3);
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    private void forceMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
