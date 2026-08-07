package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

class FrostwielderTest extends BaseCardTest {

    private boolean isExiled(String cardName) {
        return gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals(cardName));
    }

    @Test
    @DisplayName("A creature killed by the ping is exiled instead of dying")
    void pingedCreatureIsExiled() {
        addCreatureReady(player1, new Frostwielder());
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        assertThat(isExiled("Llanowar Elves")).isTrue();
    }

    @Test
    @DisplayName("A creature damaged earlier by Frostwielder is exiled when another source finishes it")
    void creatureDamagedEarlierIsExiled() {
        addCreatureReady(player1, new Frostwielder());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player2, new GrizzlyBears());

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
    @DisplayName("A creature Frostwielder never damaged dies to the graveyard normally")
    void undamagedCreatureGoesToGraveyard() {
        addCreatureReady(player1, new Frostwielder());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("The replacement stops applying once Frostwielder has left the battlefield")
    void replacementStopsWhenFrostwielderLeaves() {
        Permanent frostwielder = addCreatureReady(player1, new Frostwielder());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.getGameData().playerBattlefields.get(player1.getId()).remove(frostwielder);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
