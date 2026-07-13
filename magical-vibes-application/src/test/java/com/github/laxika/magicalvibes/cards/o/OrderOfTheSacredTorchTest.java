package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderOfTheSacredTorchTest extends BaseCardTest {

    // ===== Counters a black spell, paying 1 life =====

    @Test
    @DisplayName("Counters target black spell and pays 1 life")
    void countersBlackSpell() {
        OrderOfTheSacredTorch order = new OrderOfTheSacredTorch();
        addCreatureReady(player1, order);
        harness.setLife(player1, 20);

        GrizzlyBears victim = new GrizzlyBears();
        harness.addToBattlefield(player1, victim);

        CruelEdict edict = new CruelEdict();
        harness.setHand(player2, List.of(edict));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, edict.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Cruel Edict is countered — Grizzly Bears survives, 1 life paid
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cruel Edict"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    // ===== Cannot target a non-black spell =====

    @Test
    @DisplayName("Cannot target a red spell")
    void cannotTargetRedSpell() {
        OrderOfTheSacredTorch order = new OrderOfTheSacredTorch();
        addCreatureReady(player1, order);
        harness.setLife(player1, 20);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
