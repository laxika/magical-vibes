package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoriokReplica.class})
class MoriokReplicaTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices Moriok Replica and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addCreatureReady(player1, new MoriokReplica());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        // Moriok Replica should be sacrificed immediately (cost)
        harness.assertNotOnBattlefield(player1, "Moriok Replica");
        harness.assertInGraveyard(player1, "Moriok Replica");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Moriok Replica");
    }

    @Test
    @DisplayName("Resolving ability draws two cards and loses 2 life")
    void resolvingAbilityDrawsCardsAndLosesLife() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new MoriokReplica());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should have drawn 2 cards
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        // Should have lost 2 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new MoriokReplica());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with only colorless mana (needs black)")
    void cannotActivateWithOnlyColorlessMana() {
        addCreatureReady(player1, new MoriokReplica());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        MoriokReplica card = new MoriokReplica();
        harness.addToBattlefield(player1, card);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A tapped Replica can activate and only its controller draws and loses life")
    void tappedReplicaCanActivate() {
        Permanent replica = harness.addToBattlefieldAndReturn(player1, new MoriokReplica());
        replica.tap();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MoriokReplica(), new MoriokReplica()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Moriok Replica");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    @DisplayName("At one life the ability still draws two cards before its controller loses the game")
    void canActivateWithLessThanTwoLife() {
        harness.addToBattlefield(player1, new MoriokReplica());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MoriokReplica(), new MoriokReplica()));
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 1);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, -1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
