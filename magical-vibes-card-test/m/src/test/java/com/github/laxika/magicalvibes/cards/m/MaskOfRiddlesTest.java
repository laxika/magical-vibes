package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.EsperStormblade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaskOfRiddles.class, GrizzlyBears.class, Forest.class, EsperStormblade.class})
class MaskOfRiddlesTest extends BaseCardTest {


    @Test
    @DisplayName("Equipped creature has fear")
    void equippedCreatureHasFear() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mask = addMaskReady(player1);
        mask.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Creature loses fear when the mask is removed")
    void creatureLosesFearWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mask = addMaskReady(player1);
        mask.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(mask);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isFalse();
    }


    @Test
    @DisplayName("May draw a card when equipped creature deals combat damage to a player")
    void mayDrawOnCombatDamage() {
        Permanent creature = addAttacker(player1);
        Permanent mask = addMaskReady(player1);
        mask.setAttachedTo(creature.getId());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining draws no card")
    void decliningDrawsNothing() {
        Permanent creature = addAttacker(player1);
        Permanent mask = addMaskReady(player1);
        mask.setAttachedTo(creature.getId());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("No draw trigger when equipped creature is blocked and deals no player damage")
    void noTriggerWhenBlocked() {
        Permanent creature = addAttacker(player1);
        Permanent mask = addMaskReady(player1);
        mask.setAttachedTo(creature.getId());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        Permanent blocker = addCreatureReady(player2, new EsperStormblade());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Equip attaches the Mask for two mana and grants fear")
    void equipAttachesMask() {
        Permanent mask = addMaskReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Reequipping moves fear to the new creature")
    void reequippingMovesFear() {
        Permanent mask = addMaskReady(player1);
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        Permanent replacement = addCreatureReady(player1, new GrizzlyBears());
        mask.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, replacement.getId());
        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.hasKeyword(gd, original, Keyword.FEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Removing Mask after combat damage does not stop its draw trigger")
    void removingMaskDoesNotStopPendingDraw() {
        Permanent creature = addAttacker(player1);
        Permanent mask = addMaskReady(player1);
        mask.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        resolveCombatAndTrigger();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        gd.playerBattlefields.get(player1.getId()).remove(mask);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Unattached Mask neither grants fear nor triggers on combat damage")
    void unattachedMaskDoesNotTrigger() {
        Permanent creature = addAttacker(player1);
        addMaskReady(player1);
        harness.setHand(player1, List.of());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isFalse();
        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mask controller draws even when the equipped creature has another controller")
    void maskControllerDrawsForOpposingCreature() {
        Permanent creature = addAttacker(player1);
        Permanent mask = addMaskReady(player2);
        mask.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        resolveCombatAndTrigger();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    private Permanent addMaskReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MaskOfRiddles());
    }

    private Permanent addAttacker(Player player) {
        Permanent creature = addCreatureReady(player, new GrizzlyBears());
        creature.setAttacking(true);
        return creature;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities(); // resolve what combat damage triggered
    }
}
