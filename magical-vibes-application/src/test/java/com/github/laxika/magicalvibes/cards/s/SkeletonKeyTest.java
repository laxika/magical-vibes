package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkeletonKeyTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has skulk")
    void equippedCreatureHasSkulk() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent key = addKeyReady(player1);
        key.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SKULK)).isTrue();
    }

    @Test
    @DisplayName("Creature loses skulk when Skeleton Key is removed")
    void creatureLosesSkulkWhenKeyIsRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent key = addKeyReady(player1);
        key.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SKULK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(key);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SKULK)).isFalse();
    }

    @Test
    @DisplayName("Accepting the combat-damage trigger discards a card and draws a card")
    void acceptingCombatDamageTriggerRummages() {
        Permanent creature = addAttacker(player1);
        Permanent key = addKeyReady(player1);
        key.setAttachedTo(creature.getId());
        GrizzlyBears discarded = new GrizzlyBears();
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
        GrizzlyBears discarded = new GrizzlyBears();
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

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private Permanent addKeyReady(Player player) {
        Permanent perm = new Permanent(new SkeletonKey());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAttacker(Player player) {
        Permanent creature = addCreatureReady(player, new GrizzlyBears());
        creature.setAttacking(true);
        return creature;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
