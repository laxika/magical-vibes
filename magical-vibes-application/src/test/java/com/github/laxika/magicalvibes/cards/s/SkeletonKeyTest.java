package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkeletonKey.class, DevilthornFox.class, Forest.class, SanitariumSkeleton.class})
class SkeletonKeyTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has skulk")
    void equippedCreatureHasSkulk() {
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        Permanent key = addKeyReady(player1);
        key.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SKULK)).isTrue();
    }

    @Test
    @DisplayName("Creature loses skulk when Skeleton Key is removed")
    void creatureLosesSkulkWhenKeyIsRemoved() {
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        Permanent key = addKeyReady(player1);
        key.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SKULK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(key);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SKULK)).isFalse();
    }

    @Test
    @DisplayName("Accepting the combat-damage trigger draws a card then discards a card")
    void acceptingCombatDamageTriggerDrawsThenDiscards() {
        Permanent creature = addAttacker(player1);
        Permanent key = addKeyReady(player1);
        key.setAttachedTo(creature.getId());
        DevilthornFox discarded = new DevilthornFox();
        Forest drawn = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player1, new ArrayList<>(List.of(drawn, new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the combat-damage trigger neither discards nor draws")
    void decliningCombatDamageTriggerDoesNothing() {
        Permanent creature = addAttacker(player1);
        Permanent key = addKeyReady(player1);
        key.setAttachedTo(creature.getId());
        DevilthornFox discarded = new DevilthornFox();
        Forest drawn = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player1, new ArrayList<>(List.of(drawn)));

        resolveCombatAndTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("No trigger occurs when the equipped creature is blocked")
    void noTriggerWhenBlocked() {
        Permanent creature = addAttacker(player1);
        Permanent key = addKeyReady(player1);
        key.setAttachedTo(creature.getId());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        Permanent blocker = addCreatureReady(player2, new DevilthornFox());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void equipPaysTwoManaAndMovesSkulkToNewCreature() {
        Permanent first = addCreatureReady(player1, new DevilthornFox());
        Permanent second = addCreatureReady(player1, new DevilthornFox());
        Permanent key = addKeyReady(player1);
        key.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(key.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.hasKeyword(gd, first, Keyword.SKULK)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.SKULK)).isTrue();
    }

    @Test
    void newlyDrawnCardCanBeDiscarded() {
        Permanent creature = addAttacker(player1);
        addKeyReady(player1).setAttachedTo(creature.getId());
        DevilthornFox original = new DevilthornFox();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(original));
        harness.setLibrary(player1, List.of(drawn, new Forest()));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original, drawn);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void equipmentControllerLootsWhenOpponentControlsEquippedCreature() {
        Permanent creature = addAttacker(player1);
        addKeyReady(player2).setAttachedTo(creature.getId());
        Forest drawn = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawn, new Forest()));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void grantedSkulkPreventsGreaterPowerBlocker() {
        Permanent attacker = addCreatureReady(player1, new SanitariumSkeleton());
        attacker.setAttacking(true);
        addKeyReady(player1).setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new DevilthornFox());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
    }

    private Permanent addKeyReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SkeletonKey());
    }

    private Permanent addAttacker(Player player) {
        Permanent creature = addCreatureReady(player, new DevilthornFox());
        creature.setAttacking(true);
        return creature;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
