package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AquitectsWill;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Pestermite;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtherealWhiskergill.class, Island.class, AquitectsWill.class, Plains.class, Pestermite.class})
class EtherealWhiskergillTest extends BaseCardTest {

    @Test
    @DisplayName("Ethereal Whiskergill can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new EtherealWhiskergill());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Ethereal Whiskergill cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        addCreatureReady(player1, new EtherealWhiskergill());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ethereal Whiskergill cannot attack when only attacking player controls an Island")
    void cannotAttackWhenOnlyAttackingPlayerControlsIsland() {
        addCreatureReady(player1, new EtherealWhiskergill());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped Island still permits attacking")
    void canAttackWhenDefendersIslandIsTapped() {
        harness.addToBattlefieldAndReturn(player2, new Island()).setTapped(true);
        addCreatureReady(player1, new EtherealWhiskergill());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("An Island in the defending player's graveyard does not permit attacking")
    void cannotAttackWhenIslandIsOnlyInGraveyard() {
        harness.setGraveyard(player2, List.of(new Island()));
        addCreatureReady(player1, new EtherealWhiskergill());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Plains made an Island by Aquitect's Will permits attacking")
    void canAttackWhenDefendersLandGainsIslandSubtype() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        addCreatureReady(player1, new EtherealWhiskergill());
        harness.setHand(player1, List.of(new AquitectsWill()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, plains.getId());
        declareAttackers(List.of(0));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A Plains without the Island subtype does not permit attacking")
    void cannotAttackWhenDefenderControlsOnlyPlains() {
        harness.addToBattlefield(player2, new Plains());
        addCreatureReady(player1, new EtherealWhiskergill());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ethereal Whiskergill can block even when neither player controls an Island")
    void canBlockWithoutIslands() {
        addCreatureReady(player1, new Pestermite());
        Permanent blocker = addCreatureReady(player2, new EtherealWhiskergill());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
