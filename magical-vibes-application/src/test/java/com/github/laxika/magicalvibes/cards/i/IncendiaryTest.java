package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncendiaryTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger puts a fuse counter on Incendiary")
    void upkeepAcceptedAddsFuseCounter() {
        Permanent incendiary = addIncendiaryAttachedTo(player1, player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(incendiary.getCounterCount(CounterType.FUSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves fuse counters unchanged")
    void upkeepDeclinedAddsNoFuseCounter() {
        Permanent incendiary = addIncendiaryAttachedTo(player1, player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(incendiary.getCounterCount(CounterType.FUSE)).isZero();
    }

    @Test
    @DisplayName("When the enchanted creature dies, Incendiary deals damage equal to its fuse counters")
    void enchantedCreatureDeathDealsFuseCounterDamageToPlayer() {
        Permanent incendiary = addIncendiaryAttachedTo(player1, player1);
        incendiary.setCounterCount(CounterType.FUSE, 3);
        Permanent creature = findAttachedCreature(incendiary);
        int lifeBefore = gd.getLife(player2.getId());

        killCreature(creature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        harness.assertInGraveyard(player1, "Incendiary");
    }

    @Test
    @DisplayName("Incendiary's death trigger can target a creature")
    void enchantedCreatureDeathCanDamageCreature() {
        Permanent incendiary = addIncendiaryAttachedTo(player1, player1);
        incendiary.setCounterCount(CounterType.FUSE, 1);
        Permanent enchantedCreature = findAttachedCreature(incendiary);
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        killCreature(enchantedCreature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(targetCreature.getId(), player2.getId());
        harness.handlePermanentChosen(player1, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetCreature.getId()));
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Incendiary()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addIncendiaryAttachedTo(Player auraController, Player creatureController) {
        Permanent creature = harness.addToBattlefieldAndReturn(creatureController, new GrizzlyBears());
        Permanent incendiary = new Permanent(new Incendiary());
        incendiary.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(incendiary);
        return incendiary;
    }

    private Permanent findAttachedCreature(Permanent aura) {
        return gd.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> aura.getAttachedTo().equals(permanent.getId()))
                .findFirst()
                .orElseThrow();
    }

    private void killCreature(Permanent target) {
        target.setMarkedDamage(target.getCard().getToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();
    }
}
