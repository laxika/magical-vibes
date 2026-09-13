package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Daze.class, AirElemental.class, Island.class, Mountain.class})
class DazeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when cast for its mana cost")
    void countersSpellForManaCost() {
        AirElemental elemental = new AirElemental();
        harness.setHand(player1, List.of(elemental));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.setHand(player2, List.of(new Daze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elemental.getId());

        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInGraveyard(player2, "Daze");
    }

    @Test
    @DisplayName("Does not counter a spell when its controller pays {1}")
    void doesNotCounterWhenTargetControllerPays() {
        AirElemental elemental = new AirElemental();
        harness.setHand(player1, List.of(elemental));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.setHand(player2, List.of(new Daze()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elemental.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Air Elemental");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player2, "Daze");
    }

    @Test
    @DisplayName("Can be cast by returning an Island to its owner's hand")
    void countersSpellByReturningIsland() {
        AirElemental elemental = new AirElemental();
        harness.setHand(player1, List.of(elemental));
        harness.addMana(player1, ManaColor.BLUE, 5);

        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player2, List.of(new Daze()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithAlternateCost(player2, 0, elemental.getId(), List.of(island.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInGraveyard(player2, "Daze");
        harness.assertInHand(player2, "Island");
    }

    @Test
    @DisplayName("Alternate cost rejects a non-Island")
    void alternateCostRejectsNonIsland() {
        AirElemental elemental = new AirElemental();
        harness.setHand(player1, List.of(elemental));
        harness.addMana(player1, ManaColor.BLUE, 5);

        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player2, List.of(new Daze()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player2, 0, elemental.getId(), List.of(mountain.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost rejects an Island controlled by an opponent")
    void alternateCostRejectsOpponentsIsland() {
        AirElemental elemental = new AirElemental();
        harness.setHand(player1, List.of(elemental));
        harness.addMana(player1, ManaColor.BLUE, 5);

        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player2, List.of(new Daze()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player2, 0, elemental.getId(), List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
