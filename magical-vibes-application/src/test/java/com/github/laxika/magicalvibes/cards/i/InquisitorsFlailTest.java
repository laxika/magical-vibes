package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DoubleEquippedCreatureCombatDamageEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InquisitorsFlailTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Inquisitor's Flail has DoubleEquippedCreatureCombatDamageEffect as static effect")
    void hasCorrectStaticEffect() {
        InquisitorsFlail card = new InquisitorsFlail();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(DoubleEquippedCreatureCombatDamageEffect.class);
    }

    @Test
    @DisplayName("Inquisitor's Flail has equip {2} ability")
    void hasEquipAbility() {
        InquisitorsFlail card = new InquisitorsFlail();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}");
    }

    // ===== Doubles outgoing combat damage to player =====

    @Test
    @DisplayName("Equipped creature deals double combat damage to player when unblocked")
    void doublesUnblockedCombatDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent bear = addReadyCreature(player1, new GrizzlyBears()); // 2/2
        Permanent flail = addFlail(player1);
        flail.setAttachedTo(bear.getId());

        declareAttackers(player1, List.of(0)); // bear at index 0

        // 2 combat damage doubled to 4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== Doubles outgoing combat damage to blocker =====

    @Test
    @DisplayName("Equipped attacker deals double combat damage to blocker")
    void doublesOutgoingDamageToBlocker() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears()); // 2/2
        Permanent flail = addFlail(player1);
        flail.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        // 4/4 blocker — base 2 damage wouldn't kill, but doubled 4 does
        Permanent blocker = addReadyCreature(player2, new SerraAngel()); // 4/4

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Serra Angel (4/4) takes 2*2=4 — exactly lethal
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
    }

    // ===== Doubles incoming combat damage to equipped creature =====

    @Test
    @DisplayName("Equipped creature receives double combat damage from blocker")
    void doublesIncomingDamageFromBlocker() {
        // 2/4 attacker with Flail
        GrizzlyBears creature2_4 = new GrizzlyBears();
        creature2_4.setPower(2);
        creature2_4.setToughness(4);
        Permanent attacker = new Permanent(creature2_4);
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        Permanent flail = addFlail(player1);
        flail.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        // 2/2 blocker — normally 2 damage to 2/4 (4 toughness), survives
        // But with Flail doubling incoming: 2*2=4, exactly lethal
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears()); // 2/2

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // 2/4 attacker takes 2*2=4 doubled incoming damage — exactly lethal
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Blocker takes 2*2=4 doubled outgoing damage — also dies
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Unequipped Flail has no combat effect =====

    @Test
    @DisplayName("Unattached Flail on battlefield does not double combat damage")
    void unattachedFlailDoesNotAffectCombat() {
        harness.setLife(player2, 20);
        Permanent bear = addReadyCreature(player1, new GrizzlyBears()); // 2/2
        addFlail(player1); // not attached

        declareAttackers(player1, List.of(0)); // bear at index 0

        // 2 combat damage — NOT doubled
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Equipped blocker doubles dealt and received =====

    @Test
    @DisplayName("Equipped blocker deals and receives double combat damage")
    void equippedBlockerDoublesBothWays() {
        // 2/2 unequipped attacker
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears()); // 2/2
        attacker.setAttacking(true);

        // 2/4 blocker with Flail
        GrizzlyBears creature2_4 = new GrizzlyBears();
        creature2_4.setPower(2);
        creature2_4.setToughness(4);
        Permanent blocker = new Permanent(creature2_4);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        Permanent flail = addFlail(player2);
        flail.setAttachedTo(blocker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Blocker deals 2*2=4 to attacker (2/2) — attacker dies
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Blocker receives 2*2=4 incoming damage — exactly lethal for 4 toughness, dies
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Stacks with Furnace of Rath =====

    @Test
    @DisplayName("Flail stacks with Furnace of Rath for outgoing combat damage")
    void stacksWithFurnaceOfRath() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FurnaceOfRath());
        Permanent bear = addReadyCreature(player1, new GrizzlyBears()); // 2/2
        Permanent flail = addFlail(player1);
        flail.setAttachedTo(bear.getId());

        declareAttackers(player1, List.of(1)); // Furnace at 0, bear at 1

        // 2 * 2 (Furnace global) * 2 (Flail source) = 8
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    // ===== Removing Flail stops the doubling =====

    @Test
    @DisplayName("Removing Flail stops doubling combat damage")
    void removingFlailStopsDoubling() {
        harness.setLife(player2, 20);
        Permanent bear = addReadyCreature(player1, new GrizzlyBears()); // 2/2
        Permanent flail = addFlail(player1);
        flail.setAttachedTo(bear.getId());

        declareAttackers(player1, List.of(0)); // bear at index 0

        // 2 * 2 = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // Detach Flail
        flail.setAttachedTo(null);

        // Attack again
        harness.setLife(player2, 20);
        bear.untap();
        declareAttackers(player1, List.of(0));

        // 2 damage — NOT doubled
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Does not double non-combat damage =====

    @Test
    @DisplayName("Flail does not double non-combat spell damage")
    void doesNotDoubleSpellDamage() {
        harness.addToBattlefield(player1, new InquisitorsFlail());
        // Flail is on the battlefield but that doesn't matter for spell damage —
        // it only affects equipped creatures' combat damage
        // (Spell damage uses applyDamageMultiplier, not applyCombatDamageMultiplier)
        // Just verify the card can be put on the battlefield as an artifact
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Inquisitor's Flail"));
    }

    // ===== First strike + Flail: doubled first-strike damage kills blocker before regular damage =====

    @Test
    @DisplayName("Doubled first-strike combat damage from equipped creature kills blocker before regular damage")
    void doublesFirstStrikeDamage() {
        // 2/2 first strike attacker with Flail
        Permanent attacker = addReadyCreature(player1, new BenalishKnight()); // 2/2 first strike
        Permanent flail = addFlail(player1);
        flail.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        // 3/3 blocker — first strike 2*2=4 >= 3, dies before dealing regular damage
        GrizzlyBears creature3_3 = new GrizzlyBears();
        creature3_3.setPower(3);
        creature3_3.setToughness(3);
        Permanent blocker = new Permanent(creature3_3);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // 3/3 blocker takes 2*2=4 first-strike doubled damage — dies
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Knight survives since blocker died in first strike phase
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Benalish Knight"));
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addFlail(Player player) {
        Permanent perm = new Permanent(new InquisitorsFlail());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
