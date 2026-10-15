package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlloyGolem;
import com.github.laxika.magicalvibes.cards.b.BenalishLancer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SleepersRobe.class, Forest.class, Mountain.class, BenalishLancer.class, ShivanZombie.class, AlloyGolem.class})
class SleepersRobeTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has fear")
    void enchantedCreatureHasFear() {
        Permanent creature = addCreatureReady(player1, new BenalishLancer());
        Permanent robe = addRobeReady(player1);
        robe.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Creature loses fear when Sleeper's Robe is removed")
    void creatureLosesFearWhenRobeIsRemoved() {
        Permanent creature = addCreatureReady(player1, new BenalishLancer());
        Permanent robe = addRobeReady(player1);
        robe.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(robe);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("May draw a card when enchanted creature deals combat damage to an opponent")
    void mayDrawOnCombatDamage() {
        Permanent creature = addAttacker(player1);
        Permanent robe = addRobeReady(player1);
        robe.setAttachedTo(creature.getId());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Enchanted creature cannot be blocked by a nonblack, nonartifact creature")
    void fearPreventsNonblackCreatureFromBlocking() {
        Permanent creature = addAttacker(player1);
        Permanent robe = addRobeReady(player1);
        robe.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new BenalishLancer());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a black creature")
    void fearAllowsBlackCreatureToBlock() {
        Permanent creature = addAttacker(player1);
        Permanent robe = addRobeReady(player1);
        robe.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new ShivanZombie());

        prepareDeclareBlockers();

        assertThatCode(() -> declareBlock(blocker, creature))
                .doesNotThrowAnyException();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("No draw when enchanted opponent creature deals combat damage to the Aura's controller")
    void noDrawWhenEnchantedCreatureDamagesAuraController() {
        Permanent creature = addAttacker(player2);
        Permanent robe = addRobeReady(player1);
        robe.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        resolveCombatAndTrigger(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining the combat-damage draw draws nothing")
    void decliningDrawsNothing() {
        Permanent creature = addAttacker(player1);
        Permanent robe = addRobeReady(player1);
        robe.setAttachedTo(creature.getId());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("No draw trigger occurs when enchanted creature deals no combat damage to an opponent")
    void noTriggerWhenBlocked() {
        Permanent creature = addAttacker(player1);
        Permanent robe = addRobeReady(player1);
        robe.setAttachedTo(creature.getId());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        Permanent blocker = addCreatureReady(player2, new ShivanZombie());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Sleeper's Robe cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player2, new BenalishLancer());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new SleepersRobe()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fear allows an artifact creature to block")
    void fearAllowsArtifactCreatureToBlock() {
        Permanent creature = addAttacker(player1);
        Permanent robe = addRobeReady(player1);
        robe.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new AlloyGolem());
        blocker.setChosenColor(CardColor.WHITE);

        prepareDeclareBlockers();

        assertThatCode(() -> declareBlock(blocker, creature)).doesNotThrowAnyException();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Casting Sleeper's Robe attaches it to the targeted creature and grants fear")
    void castingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2, new BenalishLancer());
        harness.setHand(player1, List.of(new SleepersRobe()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent robe = findPermanent(player1, "Sleeper's Robe");
        assertThat(robe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isTrue();
    }

    private Permanent addRobeReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SleepersRobe());
    }

    private Permanent addAttacker(Player player) {
        Permanent creature = addCreatureReady(player, new BenalishLancer());
        creature.setAttacking(true);
        return creature;
    }

    private void resolveCombatAndTrigger() {
        resolveCombatAndTrigger(player1);
    }

    private void resolveCombatAndTrigger(Player activePlayer) {
        resolveCombat(activePlayer);
        harness.passBothPriorities();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
