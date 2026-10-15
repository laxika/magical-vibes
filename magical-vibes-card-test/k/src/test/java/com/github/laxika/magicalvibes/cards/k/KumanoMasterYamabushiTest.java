package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.List;

@CardUsed({KumanoMasterYamabushi.class, GrizzlyBears.class, LlanowarElves.class,
        ProdigalPyromancer.class})
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
    @DisplayName("The exile replacement expires at the end of the turn")
    void replacementExpiresAtEndOfTurn() {
        addCreatureReady(player1, new KumanoMasterYamabushi());
        addCreatureReady(player1, new ProdigalPyromancer());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, targetId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.activateAbility(player1, 2, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isFalse();
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

    @Test
    @DisplayName("Kumano can activate repeatedly while summoning sick")
    void canPingRepeatedlyWithoutHaste() {
        harness.addToBattlefield(player1, new KumanoMasterYamabushi());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Kumano's replacement stops applying after it leaves the battlefield")
    void replacementStopsWhenKumanoDies() {
        addCreatureReady(player1, new KumanoMasterYamabushi());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 10);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID kumanoId = harness.getPermanentId(player1, "Kumano, Master Yamabushi");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, kumanoId);
            harness.passBothPriorities();
        }
        harness.assertNotOnBattlefield(player1, "Kumano, Master Yamabushi");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isFalse();
    }

    @Test
    @CardUsed({Humble.class})
    @DisplayName("Losing Kumano's abilities disables its exile replacement")
    void replacementStopsWhenKumanoLosesAbilities() {
        addCreatureReady(player1, new KumanoMasterYamabushi());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Kumano, Master Yamabushi"));
        harness.activateAbility(player1, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isFalse();
    }
}
