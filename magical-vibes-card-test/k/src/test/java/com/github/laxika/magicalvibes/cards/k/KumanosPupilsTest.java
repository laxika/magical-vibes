package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KumanosPupils.class, GrizzlyBears.class, ProdigalPyromancer.class,
        GiantSpider.class, LlanowarElves.class, Humble.class, GiantGrowth.class, Terror.class})
class KumanosPupilsTest extends BaseCardTest {

    private boolean isExiled(String cardName) {
        return gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals(cardName));
    }

    @Test
    @DisplayName("A blocker killed by combat damage from Kumano's Pupils is exiled instead of dying")
    void blockerKilledInCombatIsExiled() {
        addCreatureReady(player1, new KumanosPupils());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("A creature damaged by Kumano's Pupils is exiled when another source finishes it")
    void creatureDamagedEarlierIsExiled() {
        addCreatureReady(player1, new KumanosPupils());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.assertOnBattlefield(player2, "Giant Spider");

        UUID targetId = harness.getPermanentId(player2, "Giant Spider");
        harness.activateAbility(player1, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertNotInGraveyard(player2, "Giant Spider");
        assertThat(isExiled("Giant Spider")).isTrue();
    }

    @Test
    @DisplayName("The replacement stops applying once Kumano's Pupils leaves the battlefield")
    void replacementStopsWhenPupilsLeaves() {
        var pupils = addCreatureReady(player1, new KumanosPupils());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        UUID targetId = harness.getPermanentId(player2, "Giant Spider");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, pupils));

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(isExiled("Giant Spider")).isFalse();
    }

    @Test
    @DisplayName("A creature Kumano's Pupils never damaged dies to the graveyard normally")
    void undamagedCreatureGoesToGraveyard() {
        addCreatureReady(player1, new KumanosPupils());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Pupils that lost all abilities does not exile a creature it kills")
    void losingAbilitiesDisablesReplacement() {
        var pupils = addCreatureReady(player1, new KumanosPupils());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Humble(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, pupils.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, pupils.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Kumano's Pupils");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isFalse();
    }

    @Test
    @DisplayName("Pupils dying simultaneously still exiles the creature it damaged")
    void simultaneousCombatDeathsAreBothExiled() {
        addCreatureReady(player1, new KumanosPupils());
        harness.addToBattlefield(player2, new KumanosPupils());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Kumano's Pupils");
        harness.assertNotOnBattlefield(player2, "Kumano's Pupils");
        harness.assertNotInGraveyard(player1, "Kumano's Pupils");
        harness.assertNotInGraveyard(player2, "Kumano's Pupils");
        assertThat(gd.exiledCards.stream()
                .filter(e -> e.card().getName().equals("Kumano's Pupils")).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("A damaged creature destroyed later that turn is exiled")
    void destructionAfterDamageIsReplaced() {
        addCreatureReady(player1, new KumanosPupils());
        harness.addToBattlefield(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.assertOnBattlefield(player2, "Giant Spider");

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Giant Spider"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertNotInGraveyard(player2, "Giant Spider");
        assertThat(isExiled("Giant Spider")).isTrue();
    }

    @Test
    @DisplayName("Damage from a previous turn does not cause exile")
    void damageHistoryExpiresAtTurnBoundary() {
        addCreatureReady(player1, new KumanosPupils());
        harness.addToBattlefield(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.assertOnBattlefield(player2, "Giant Spider");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Giant Spider"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kumano's Pupils");
        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(isExiled("Giant Spider")).isFalse();
    }
}
