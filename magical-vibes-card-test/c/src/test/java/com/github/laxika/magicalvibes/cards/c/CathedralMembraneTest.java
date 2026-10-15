package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Dismember;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LabyrinthOfSkophos;
import com.github.laxika.magicalvibes.cards.n.NeurokCommando;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CathedralMembrane.class, GrizzlyBears.class, WrathOfGod.class,
        Dismember.class, LabyrinthOfSkophos.class, NeurokCommando.class, PhyrexianHulk.class})
class CathedralMembraneTest extends BaseCardTest {

    /**
     * Sets up combat where Cathedral Membrane (player2, defender) blocks a creature (player1, attacker).
     * Player1 attacks with a creature at index 0, and Cathedral Membrane blocks it.
     */
    private void setupCombatWhereMembraneBlocks(Permanent attackerPerm, Permanent membranePerm) {
        attackerPerm.setSummoningSick(false);
        attackerPerm.setAttacking(true);

        membranePerm.setSummoningSick(false);
        membranePerm.setBlocking(true);
        membranePerm.addBlockingTarget(0);
        membranePerm.addBlockingTargetId(attackerPerm.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("When Cathedral Membrane dies in combat, it deals 6 damage to the creature it blocked")
    void deathTriggerDeals6DamageToBlockedCreature() {
        GrizzlyBears attacker = new GrizzlyBears();
        attacker.setPower(3);
        attacker.setToughness(3);
        Permanent attackerPerm = harness.addToBattlefieldAndReturn(player1, attacker);
        Permanent membranePerm = harness.addToBattlefieldAndReturn(player2, new CathedralMembrane());

        UUID attackerId = attackerPerm.getId();
        setupCombatWhereMembraneBlocks(attackerPerm, membranePerm);

        // Pass priority to deal combat damage — Membrane (0/3) dies to the 3/3 attacker
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Cathedral Membrane should be dead
        harness.assertInGraveyard(player2, "Cathedral Membrane");

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Cathedral Membrane")
                && e.getTargetIds().contains(attackerId));

        // Resolve the triggered ability — 6 damage to a 3/3 is lethal
        harness.passBothPriorities();

        // The attacker should be destroyed by the 6 damage
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(attackerId));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cathedral Membrane deals 6 damage but does not kill a creature with toughness > 6")
    void deathTriggerDoesNotKillHighToughnessCreature() {
        GrizzlyBears bigAttacker = new GrizzlyBears();
        bigAttacker.setPower(3);
        bigAttacker.setToughness(7);
        Permanent attackerPerm = harness.addToBattlefieldAndReturn(player1, bigAttacker);
        Permanent membranePerm = harness.addToBattlefieldAndReturn(player2, new CathedralMembrane());

        UUID attackerId = attackerPerm.getId();
        setupCombatWhereMembraneBlocks(attackerPerm, membranePerm);

        harness.passBothPriorities(); // Combat damage — Membrane dies
        harness.passBothPriorities(); // Resolve trigger — 6 damage to 7 toughness is not lethal

        GameData gd = harness.getGameData();

        // Attacker should still be alive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(attackerId));
    }

    @Test
    @DisplayName("Cathedral Membrane does not trigger when it dies outside of combat (Wrath of God)")
    void noTriggerOutsideCombat() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new CathedralMembrane());

        // Use Wrath of God to kill Cathedral Membrane outside of combat
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities(); // Resolve Wrath — all creatures die

        GameData gd = harness.getGameData();

        // Cathedral Membrane should be dead
        harness.assertInGraveyard(player2, "Cathedral Membrane");

        // No Cathedral Membrane triggered ability should be on the stack
        // (Membrane died during precombat main, not during combat)
        assertThat(gd.stack).noneMatch(e ->
                e.getCard().getName().equals("Cathedral Membrane"));
    }

    @Test
    void deathDuringCombatTriggersEvenWithoutBlocking() {
        Permanent membrane = harness.addToBattlefieldAndReturn(player2, new CathedralMembrane());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT,
                () -> harness.castAndResolveInstant(player1, 0, membrane.getId()));

        harness.assertInGraveyard(player2, "Cathedral Membrane");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Cathedral Membrane"));
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void deathDamageDoesNotTargetShroudedBlockedCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new NeurokCommando());
        Permanent membrane = harness.addToBattlefieldAndReturn(player2, new CathedralMembrane());
        setupCombatWhereMembraneBlocks(attacker, membrane);
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, membrane.getId());
        harness.assertInGraveyard(player2, "Cathedral Membrane");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Neurok Commando");
        harness.assertNotOnBattlefield(player1, "Neurok Commando");
    }

    @Test
    void remembersBlockedCreatureAfterMembraneIsRemovedFromCombat() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PhyrexianHulk());
        Permanent membrane = harness.addToBattlefieldAndReturn(player2, new CathedralMembrane());
        harness.addToBattlefield(player2, new LabyrinthOfSkophos());
        setupCombatWhereMembraneBlocks(attacker, membrane);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.ensurePriority(player2);
        harness.activateAbility(player2, 1, 1, null, membrane.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);
        assertThat(membrane.isBlocking()).isFalse();
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, membrane.getId());
        harness.assertInGraveyard(player2, "Cathedral Membrane");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Phyrexian Hulk");
        harness.assertNotOnBattlefield(player1, "Phyrexian Hulk");
    }
}
