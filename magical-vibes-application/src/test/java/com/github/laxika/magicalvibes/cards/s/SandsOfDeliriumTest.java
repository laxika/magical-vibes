package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
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

@CardUsed({SandsOfDelirium.class})
class SandsOfDeliriumTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills X cards")
    void millsXCards() {
        Permanent sands = addReadySands(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int before = trimDeck(player2, 10);

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(before - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(sands.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Milled cards come from the top of the library")
    void millsFromTop() {
        addReadySands(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        trimDeck(player2, 5);
        List<Card> deck = gd.playerDecks.get(player2.getId());
        Card first = deck.get(0);
        Card second = deck.get(1);
        Card third = deck.get(2);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isEqualTo(third);
    }

    @Test
    @DisplayName("X of 0 mills nothing")
    void zeroMillsNothing() {
        addReadySands(player1);
        int before = trimDeck(player2, 10);

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(before);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Controller may target themselves")
    void canTargetSelf() {
        addReadySands(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int before = trimDeck(player1, 10);

        harness.activateAbility(player1, 0, 2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(before - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mill is capped by library size")
    void millCappedByLibrarySize() {
        addReadySands(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        trimDeck(player2, 3);

        harness.activateAbility(player1, 0, 5, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A newly entered noncreature artifact can activate immediately")
    void canActivateImmediately() {
        Permanent sands = harness.addToBattlefieldAndReturn(player1, new SandsOfDelirium());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Card top = new SandsOfDelirium();
        harness.setLibrary(player2, List.of(top, new SandsOfDelirium()));

        harness.activateAbility(player1, 0, 1, player2.getId());

        assertThat(sands.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("A tapped Sands cannot activate even for X zero")
    void cannotActivateWhenTapped() {
        Permanent sands = addReadySands(player1);
        sands.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("X must be paid before the ability can activate")
    void cannotActivateWithoutEnoughMana() {
        Permanent sands = addReadySands(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sands.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Milling an empty library does not make the player lose")
    void canMillEmptyLibrary() {
        addReadySands(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The ability keeps its chosen X after Sands leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent sands = addReadySands(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card first = new SandsOfDelirium();
        Card second = new SandsOfDelirium();
        Card third = new SandsOfDelirium();
        harness.setLibrary(player2, List.of(first, second, third));

        harness.activateAbility(player1, 0, 2, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sands);
        harness.setExile(player1, List.of(sands.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
    }

    private Permanent addReadySands(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SandsOfDelirium());
        perm.setSummoningSick(false);
        return perm;
    }

    private int trimDeck(Player player, int size) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        int retained = Math.min(size, deck.size());
        harness.setLibrary(player, deck.subList(deck.size() - retained, deck.size()));
        return retained;
    }
}
