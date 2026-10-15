package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.MossKami;
import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KodamaOfTheNorthTree.class, MossKami.class, RendSpirit.class})
class KodamaOfTheNorthTreeTest extends BaseCardTest {

    @Test
    @DisplayName("Shroud prevents spells from targeting Kodama of the North Tree")
    void shroudPreventsSpellsFromTargetingKodama() {
        Permanent kodama = harness.addToBattlefieldAndReturn(player2, new KodamaOfTheNorthTree());
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, kodama.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has shroud");
    }

    @Test
    @DisplayName("Shroud also prevents its controller from targeting Kodama")
    void shroudPreventsControllerFromTargetingKodama() {
        Permanent kodama = harness.addToBattlefieldAndReturn(player1, new KodamaOfTheNorthTree());
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, kodama.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has shroud");
    }

    @Test
    @DisplayName("Trample requires lethal damage to the blocker before damage to the player")
    void trampleRequiresLethalDamageToBlocker() {
        addCreatureReady(player1, new KodamaOfTheNorthTree());
        Permanent blocker = addCreatureReady(player2, new MossKami());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 4,
                player2.getId(), 2
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must assign at least 5 damage");

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 5,
                player2.getId(), 1
        ));

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new KodamaOfTheNorthTree());
        Permanent blocker = addCreatureReady(player2, new MossKami());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 5,
                player2.getId(), 1
        ));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
