package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

@CardUsed({CanopyCover.class, GrizzlyBears.class, AvenFisher.class, GiantSpider.class,
        GiantGrowth.class, ProdigalPyromancer.class})
class CanopyCoverTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot be blocked by a creature without flying or reach")
    void cannotBeBlockedByCreatureWithoutFlyingOrReach() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        enchant(attacker);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying or reach");
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by creatures with flying or reach")
    void canBeBlockedByFlyingOrReach() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        enchant(attacker);
        Permanent flyingBlocker = addCreatureReady(player2, new AvenFisher());
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(flyingBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(flyingBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a creature with reach")
    void canBeBlockedByReach() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        enchant(attacker);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature cannot be targeted by opponents' spells or abilities")
    void cannotBeTargetedByOpponentsSpellsOrAbilities() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        enchant(target);

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature can be targeted by its controller")
    void canBeTargetedByItsController() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        enchant(target);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Canopy Cover resolves attached to an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CanopyCover()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Canopy Cover").getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Aura controller's opponent cannot target their own enchanted creature with a spell")
    void creatureControllerCannotTargetWithSpellWhenAuraIsOpponents() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        enchant(target);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Aura controller can target an opponent's enchanted creature with a spell")
    void auraControllerCanTargetOpponentsCreatureWithSpell() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        enchant(target);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Aura controller's opponent cannot target their own enchanted creature with an ability")
    void creatureControllerCannotTargetWithAbilityWhenAuraIsOpponents() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        enchant(target);
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Aura controller can target an opponent's enchanted creature with an ability")
    void auraControllerCanTargetOpponentsCreatureWithAbility() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        enchant(target);
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(pyromancer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    private void enchant(Permanent creature) {
        Permanent aura = new Permanent(new CanopyCover());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

}
