package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfFire;
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

@CardUsed({Invisibility.class, GrizzlyBears.class, AvenFisher.class, WallOfFire.class, FountainOfYouth.class})
class InvisibilityTest extends BaseCardTest {

    // ===== Casting and attaching =====

    @Test
    @DisplayName("Resolving Invisibility attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Invisibility()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Invisibility")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
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

        Permanent wall = addCreatureReady(player2, new WallOfFire());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Invisibility()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attackingEnchantedCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent aura = new Permanent(new Invisibility());
        aura.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        return attacker;
    }
}
