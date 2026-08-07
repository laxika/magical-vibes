package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

class KumanoMasterYamabushiTest extends BaseCardTest {

    private boolean isExiled(String cardName) {
        return gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals(cardName));
    }

    @Test
    @DisplayName("A creature killed by the ping is exiled instead of dying")
    void pingedCreatureIsExiled() {
        addCreatureReady(player1, new KumanoMasterYamabushi());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        assertThat(isExiled("Llanowar Elves")).isTrue();
    }

    @Test
    @DisplayName("A creature Kumano damaged earlier is exiled when another source finishes it")
    void creatureDamagedEarlierIsExiled() {
        addCreatureReady(player1, new KumanoMasterYamabushi());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.activateAbility(player1, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("A creature Kumano never damaged dies to the graveyard normally")
    void undamagedCreatureGoesToGraveyard() {
        addCreatureReady(player1, new KumanoMasterYamabushi());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("The ping can hit a player")
    void pingCanHitPlayer() {
        addCreatureReady(player1, new KumanoMasterYamabushi());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }
}
