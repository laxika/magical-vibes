package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AxebaneGuardian;
import com.github.laxika.magicalvibes.cards.t.ToweringIndrik;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({Doorkeeper.class, AxebaneGuardian.class, ToweringIndrik.class})
class DoorkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Mills one card when Doorkeeper is the only creature with defender")
    void millsOneForItself() {
        addCreatureReady(player1, new Doorkeeper());
        int deckSizeBefore = trimDeck(player2);

        activate(player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Mill amount counts every creature you control with defender")
    void millsPerDefender() {
        addCreatureReady(player1, new Doorkeeper());
        harness.addToBattlefield(player1, new AxebaneGuardian());
        harness.addToBattlefield(player1, new ToweringIndrik());
        int deckSizeBefore = trimDeck(player2);

        activate(player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Defenders controlled by the opponent are not counted")
    void ignoresOpponentDefenders() {
        addCreatureReady(player1, new Doorkeeper());
        harness.addToBattlefield(player2, new AxebaneGuardian());
        int deckSizeBefore = trimDeck(player2);

        activate(player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Can target yourself with the mill ability")
    void canTargetSelf() {
        addCreatureReady(player1, new Doorkeeper());
        int deckSizeBefore = trimDeck(player1);

        activate(player1.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent doorkeeper = addCreatureReady(player1, new Doorkeeper());
        doorkeeper.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(doorkeeper.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate an already tapped Doorkeeper")
    void cannotActivateWhileTapped() {
        Permanent doorkeeper = addCreatureReady(player1, new Doorkeeper());
        doorkeeper.tap();
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the blue requirement with only green mana")
    void requiresBlueMana() {
        Permanent doorkeeper = addCreatureReady(player1, new Doorkeeper());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(doorkeeper.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void activate(java.util.UUID targetId) {
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
    }

    private int trimDeck(Player player) {
        harness.setLibrary(player, List.of(new ToweringIndrik(), new ToweringIndrik(),
                new ToweringIndrik(), new ToweringIndrik(), new ToweringIndrik()));
        return 5;
    }

    @Test
    @DisplayName("Counts defenders that enter after activation")
    void countsDefendersAtResolution() {
        addCreatureReady(player1, new Doorkeeper());
        trimDeck(player2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.addToBattlefield(player1, new AxebaneGuardian());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A departed Doorkeeper is not counted but its ability still resolves")
    void resolvesAfterSourceLeaves() {
        Permanent doorkeeper = addCreatureReady(player1, new Doorkeeper());
        harness.addToBattlefield(player1, new AxebaneGuardian());
        trimDeck(player2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(doorkeeper);
        gd.playerGraveyards.get(player1.getId()).add(doorkeeper.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Mills zero when no defenders remain at resolution")
    void millsZeroWithoutDefenders() {
        Permanent doorkeeper = addCreatureReady(player1, new Doorkeeper());
        trimDeck(player2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(doorkeeper);
        gd.playerGraveyards.get(player1.getId()).add(doorkeeper.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Mills only the remaining library cards when X exceeds its size")
    void millsShortLibrary() {
        addCreatureReady(player1, new Doorkeeper());
        harness.addToBattlefield(player1, new AxebaneGuardian());
        Card lastCard = new ToweringIndrik();
        harness.setLibrary(player2, List.of(lastCard));

        activate(player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(lastCard);
    }

    @Test
    @DisplayName("Pays two generic mana and one blue mana and taps as an activation cost")
    void paysManaAndTapCosts() {
        Permanent doorkeeper = addCreatureReady(player1, new Doorkeeper());
        trimDeck(player2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(doorkeeper.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }
}
