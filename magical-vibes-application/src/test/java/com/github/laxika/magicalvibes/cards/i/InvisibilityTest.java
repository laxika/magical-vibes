package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Invisibility.class, GrizzlyBears.class, AvenFisher.class, WallOfStone.class, HowlingMine.class})
class InvisibilityTest extends BaseCardTest {

    // ===== Casting and attaching =====

    @Test
    @DisplayName("Resolving Invisibility attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        Invisibility invisibility = new Invisibility();
        harness.setHand(player1, List.of(invisibility));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == invisibility
                        && p.isAttached()
                        && p.getAttachedTo().equals(target.getId()));
    }

    // ===== Block restriction =====

    @Test
    @DisplayName("Enchanted creature cannot be blocked by a normal creature")
    void cannotBeBlockedByNormalCreature() {
        attackingEnchantedCreature();

        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Walls");
    }

    @Test
    @DisplayName("Flying is not enough to block the enchanted creature")
    void flyingCannotBlock() {
        attackingEnchantedCreature();

        addCreatureReady(player2, new AvenFisher());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Walls");
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a Wall")
    void canBeBlockedByWall() {
        attackingEnchantedCreature();

        Permanent wall = addCreatureReady(player2, new WallOfStone());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.setHand(player1, List.of(new Invisibility()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Invisibility restricts only its enchanted creature")
    void restrictsOnlyEnchantedCreature() {
        attackingEnchantedCreature();

        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        otherAttacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(otherAttacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Removing Invisibility removes the blocking restriction")
    void restrictionEndsWhenAuraLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent aura = attachInvisibility(attacker);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent attackingEnchantedCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        attachInvisibility(attacker);

        return attacker;
    }

    private Permanent attachInvisibility(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Invisibility());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
