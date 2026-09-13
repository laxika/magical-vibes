package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GorillaWarrior.class, WornPowerstone.class, Pacifism.class})
class PacifismTest extends BaseCardTest {


    @Test
    @DisplayName("Casting Pacifism puts it on the stack")
    void castingPutsOnStack() {
        Permanent turtlePerm = addCreatureReady(player2, new GorillaWarrior());
        Pacifism pacifism = new Pacifism();

        harness.setHand(player1, List.of(pacifism));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, turtlePerm.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard()).isSameAs(pacifism);
    }

    @Test
    @DisplayName("Resolving Pacifism attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent turtlePerm = addCreatureReady(player2, new GorillaWarrior());
        Pacifism pacifism = new Pacifism();

        harness.setHand(player1, List.of(pacifism));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, turtlePerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == pacifism
                        && p.isAttached()
                        && p.getAttachedTo().equals(turtlePerm.getId()));
    }

    @Test
    @DisplayName("Cannot cast Pacifism without enough mana")
    void cannotCastWithoutEnoughMana() {
        Permanent turtlePerm = addCreatureReady(player2, new GorillaWarrior());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, turtlePerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }


    @Test
    @DisplayName("Creature enchanted with Pacifism cannot be declared as attacker")
    void enchantedCreatureCannotAttack() {
        Permanent turtlePerm = addCreatureReady(player1, new GorillaWarrior());

        attachPacifism(player2, turtlePerm);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Pacified creature is excluded from attackable creature indices")
    void pacifiedCreatureNotInAttackableIndices() {
        Permanent turtlePerm = addCreatureReady(player1, new GorillaWarrior());
        addCreatureReady(player1, new GorillaWarrior());

        attachPacifism(player2, turtlePerm);

        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player1.getId()))
                .containsExactly(1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        declareAttackers(player1, List.of(1));

        assertThat(turtlePerm.isAttacking()).isFalse();
    }


    @Test
    @DisplayName("Creature enchanted with Pacifism cannot be declared as blocker")
    void enchantedCreatureCannotBlock() {
        Permanent blockerPerm = addCreatureReady(player2, new GorillaWarrior());

        attachPacifism(player1, blockerPerm);

        Permanent atkPerm = addCreatureReady(player1, new GorillaWarrior());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Pacified creature is excluded from blockable creature indices")
    void pacifiedCreatureNotInBlockableIndices() {
        Permanent pacifiedPerm = addCreatureReady(player2, new GorillaWarrior());
        Permanent freePerm = addCreatureReady(player2, new GorillaWarrior());

        attachPacifism(player1, pacifiedPerm);

        Permanent atkPerm = addCreatureReady(player1, new GorillaWarrior());
        atkPerm.setAttacking(true);

        assertThat(harness.getCombatBlockService().getBlockableCreatureIndices(gd, player2.getId()))
                .containsExactly(1);

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, attackerIndex)));

        assertThat(freePerm.isBlocking()).isTrue();
        assertThat(pacifiedPerm.isBlocking()).isFalse();
    }


    @Test
    @DisplayName("Creature can attack again after Pacifism is removed")
    void creatureCanAttackAfterPacifismRemoved() {
        Permanent turtlePerm = addCreatureReady(player1, new GorillaWarrior());

        Permanent pacifismPerm = attachPacifism(player2, turtlePerm);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        gd.playerBattlefields.get(player2.getId()).remove(pacifismPerm);

        declareAttackers(player1, List.of(0));
    }

    @Test
    @DisplayName("Creature can block again after Pacifism is removed")
    void creatureCanBlockAfterPacifismRemoved() {
        Permanent blockerPerm = addCreatureReady(player2, new GorillaWarrior());

        Permanent pacifismPerm = attachPacifism(player1, blockerPerm);

        Permanent atkPerm = addCreatureReady(player1, new GorillaWarrior());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");

        gd.playerBattlefields.get(player1.getId()).remove(pacifismPerm);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }


    @Test
    @DisplayName("Pacifism can be cast on own creature")
    void canCastOnOwnCreature() {
        Permanent turtlePerm = addCreatureReady(player1, new GorillaWarrior());
        Pacifism pacifism = new Pacifism();

        harness.setHand(player1, List.of(pacifism));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, turtlePerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == pacifism
                        && p.isAttached()
                        && p.getAttachedTo().equals(turtlePerm.getId()));
    }


    @Test
    @DisplayName("Pacifism fizzles to graveyard if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent turtlePerm = addCreatureReady(player2, new GorillaWarrior());
        Pacifism pacifism = new Pacifism();

        harness.setHand(player1, List.of(pacifism));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, turtlePerm.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, turtlePerm));

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pacifism);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() == pacifism);
    }


    @Test
    @DisplayName("Can target a creature with Pacifism")
    void canTargetCreature() {
        Permanent turtle = addCreatureReady(player1, new GorillaWarrior());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, turtle.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Pacifism")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }


    @Test
    @DisplayName("Player with only pacified creatures can declare no attackers")
    void canDeclareNoAttackersWithOnlyPacifiedCreatures() {
        Permanent turtlePerm = addCreatureReady(player1, new GorillaWarrior());

        attachPacifism(player2, turtlePerm);

        declareAttackers(player1, List.of());

        assertThat(turtlePerm.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Pacifism is put into its owner's graveyard when its enchanted creature leaves")
    void goesToGraveyardWhenEnchantedCreatureLeaves() {
        Permanent turtlePerm = addCreatureReady(player2, new GorillaWarrior());
        Pacifism pacifism = new Pacifism();
        harness.setHand(player1, List.of(pacifism));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, turtlePerm.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, turtlePerm));
        harness.runStateBasedActions();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pacifism);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() == pacifism);
    }

    private Permanent attachPacifism(com.github.laxika.magicalvibes.model.Player controller,
                                     Permanent creature) {
        Permanent pacifism = harness.addToBattlefieldAndReturn(controller, new Pacifism());
        pacifism.setAttachedTo(creature.getId());
        return pacifism;
    }
}
