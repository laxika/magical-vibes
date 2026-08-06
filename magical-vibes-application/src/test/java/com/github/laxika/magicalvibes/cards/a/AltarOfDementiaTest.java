package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AltarOfDementiaTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills cards equal to the sacrificed creature's power")
    void millsEqualToSacrificedPower() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        addCreatureReady(player1, new SerraAngel()); // 4/4
        trimDeck(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Uses the sacrificed creature's effective (boosted) power")
    void usesEffectivePower() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // power 3
        trimDeck(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        addCreatureReady(player1, new EliteVanguard()); // 2/1
        trimDeck(player1, 10);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(3); // 2 milled cards plus the sacrificed Elite Vanguard
    }

    @Test
    @DisplayName("Mill is capped by library size")
    void millCappedByLibrarySize() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        addCreatureReady(player1, new SerraAngel()); // 4 power
        trimDeck(player2, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Controller chooses which creature to sacrifice")
    void choosesAmongCreatures() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        addCreatureReady(player1, new SerraAngel()); // 4/4
        addCreatureReady(player1, new EliteVanguard()); // 2/1
        UUID vanguard = harness.getPermanentId(player1, "Elite Vanguard");
        trimDeck(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, vanguard);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Elite Vanguard");
        harness.assertOnBattlefield(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        harness.addToBattlefield(player1, new AltarOfDementia());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick creature can still be sacrificed")
    void summoningSickCreatureCanBeSacrificed() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        harness.addToBattlefield(player1, new SerraAngel());
        trimDeck(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    private void trimDeck(com.github.laxika.magicalvibes.model.Player player, int size) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        while (deck.size() > size) {
            deck.removeFirst();
        }
    }
}
