package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CateranSlaver.class, CateranEnforcer.class, CateranOverlord.class,
        CeremonialGuard.class, Swamp.class})
class CateranSlaverTest extends BaseCardTest {

    @Test
    void searchesForMercenaryPermanentWithManaValueAtMostFive() {
        addCreatureReady(player1, new CateranSlaver());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.setLibrary(player1, List.of(new CateranEnforcer(), new CateranOverlord(), new CeremonialGuard()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Cateran Enforcer");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Cateran Enforcer");
        harness.assertNotOnBattlefield(player1, "Cateran Overlord");
        harness.assertNotOnBattlefield(player1, "Ceremonial Guard");
    }

    @Test
    void resolvesWithoutInteractionWhenNoEligibleMercenaryPermanentExists() {
        Permanent slaver = addCreatureReady(player1, new CateranSlaver());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setLibrary(player1, List.of(new CateranOverlord(), new CeremonialGuard()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(slaver.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Cateran Overlord", "Ceremonial Guard");
    }

    @Test
    void cannotActivateWithoutFiveGenericMana() {
        Permanent slaver = addCreatureReady(player1, new CateranSlaver());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(slaver.isTapped()).isFalse();
    }

    @Test
    void cannotBeBlockedWhenDefenderControlsSwamp() {
        harness.addToBattlefield(player2, new Swamp());

        Permanent blockerPerm = addCreatureReady(player2, new CeremonialGuard());

        Permanent attackerPerm = addCreatureReady(player1, new CateranSlaver());
        attackerPerm.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerPerm);

        assertThatThrownBy(() -> gs.declareBlockers(
                        gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void canBeBlockedWhenDefenderControlsNoSwamp() {
        Permanent blockerPerm = addCreatureReady(player2, new CeremonialGuard());

        Permanent attackerPerm = addCreatureReady(player1, new CateranSlaver());
        attackerPerm.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerPerm);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
