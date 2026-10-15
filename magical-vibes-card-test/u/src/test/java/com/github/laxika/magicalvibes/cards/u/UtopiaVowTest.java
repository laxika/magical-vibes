package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.e.EssenceWarden;
import com.github.laxika.magicalvibes.cards.s.SealOfPrimordium;
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

@CardUsed({UtopiaVow.class, EssenceWarden.class, SealOfPrimordium.class})
class UtopiaVowTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot be declared as an attacker")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new EssenceWarden());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new UtopiaVow());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot be declared as a blocker")
    void enchantedCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new EssenceWarden());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new EssenceWarden());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UtopiaVow());
        aura.setAttachedTo(blocker.getId());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature can tap to add one mana of any color")
    void enchantedCreatureAddsAnyColorMana() {
        Permanent creature = addCreatureReady(player1, new EssenceWarden());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new UtopiaVow());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Utopia Vow effects stop when it leaves the battlefield")
    void effectsStopWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new EssenceWarden());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UtopiaVow());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature can attack after Utopia Vow leaves the battlefield")
    void enchantedCreatureCanAttackAfterRemoved() {
        Permanent creature = addCreatureReady(player1, new EssenceWarden());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new UtopiaVow());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(aura);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Utopia Vow can target only a creature")
    void cannotTargetNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new SealOfPrimordium());
        harness.setHand(player1, List.of(new UtopiaVow()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolving Utopia Vow on an opposing creature grants mana to that creature's controller")
    void resolvedAuraGrantsManaToCreatureController() {
        Permanent creature = addCreatureReady(player2, new EssenceWarden());
        harness.setHand(player1, List.of(new UtopiaVow()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Utopia Vow").getAttachedTo()).isEqualTo(creature.getId());
        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the granted tap ability")
    void summoningSicknessPreventsGrantedManaAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EssenceWarden());
        creature.setSummoningSick(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new UtopiaVow());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped enchanted creature cannot activate the granted mana ability again")
    void tappedCreatureCannotActivateGrantedManaAbility() {
        Permanent creature = addCreatureReady(player1, new EssenceWarden());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new UtopiaVow());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }
}
