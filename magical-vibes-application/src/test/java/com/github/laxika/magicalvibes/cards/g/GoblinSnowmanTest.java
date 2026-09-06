package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.b.BraveTheSands;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinSnowman.class, BalduvianBears.class, BraveTheSands.class, ZuranSpellcaster.class})
class GoblinSnowmanTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking prevents all combat damage dealt to and by Goblin Snowman")
    void blockingPreventsCombatDamageBothWays() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        Permanent snowman = addCreatureReady(player2, new GoblinSnowman());

        blockWithSnowman();
        resolveAllTriggers();
        resolveCombat();

        assertThat(snowman.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Goblin Snowman");
        harness.assertOnBattlefield(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("A nonblocking Goblin Snowman does not create a block trigger")
    void nonblockingSnowmanDoesNotTrigger() {
        addCreatureReady(player1, new BalduvianBears());
        Permanent snowman = addCreatureReady(player2, new GoblinSnowman());
        declareAttackers(List.of(0));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(snowman.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Goblin Snowman deals and receives combat damage when it attacks")
    void attackingSnowmanHasNormalCombatDamage() {
        addCreatureReady(player1, new GoblinSnowman());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Goblin Snowman");
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability deals 1 damage to the creature Goblin Snowman is blocking")
    void tapAbilityDamagesBlockedCreature() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player2, new GoblinSnowman());

        blockWithSnowman();
        resolveAllTriggers();
        harness.activateAbility(player2, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability cannot target a creature Goblin Snowman isn't blocking")
    void tapAbilityCannotTargetUnblockedCreature() {
        addCreatureReady(player1, new BalduvianBears());
        Permanent otherAttacker = addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player2, new GoblinSnowman());

        declareAttackers(List.of(0, 1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, otherAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability cannot target a noncreature permanent")
    void tapAbilityCannotTargetNoncreature() {
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player2, new GoblinSnowman());
        harness.addToBattlefield(player1, new BraveTheSands());

        blockWithSnowman();
        resolveAllTriggers();

        Permanent enchantment = findPermanent(player1, "Brave the Sands");
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncombat damage to Goblin Snowman is not prevented after it blocks")
    void nonCombatDamageToSnowmanIsNotPrevented() {
        addCreatureReady(player1, new BalduvianBears());
        Permanent spellcaster = addCreatureReady(player1, new ZuranSpellcaster());
        Permanent snowman = addCreatureReady(player2, new GoblinSnowman());

        blockWithSnowman();
        resolveAllTriggers();

        harness.activateAbility(player1, 1, null, snowman.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goblin Snowman");
        harness.assertOnBattlefield(player1, "Balduvian Bears");
        assertThat(spellcaster.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blocking multiple creatures triggers Goblin Snowman's block ability only once")
    void blockingMultipleCreaturesTriggersOnlyOnce() {
        Permanent snowman = addCreatureReady(player2, new GoblinSnowman());
        harness.addToBattlefield(player2, new BraveTheSands());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new BalduvianBears());

        declareAttackers(List.of(0, 1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)
        ));

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .filter(entry -> snowman.getId().equals(entry.getSourcePermanentId()))
                .count()).isEqualTo(1);
    }

    /** Declares player1's first creature as an attacker and blocks it with player2's Goblin Snowman. */
    private void blockWithSnowman() {
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
