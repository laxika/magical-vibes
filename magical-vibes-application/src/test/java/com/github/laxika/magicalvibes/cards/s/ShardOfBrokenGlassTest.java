package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShardOfBrokenGlassTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shard = addShard(player1);
        shard.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("May mill two cards when equipped creature attacks")
    void acceptsAttackTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shard = addShard(player1);
        shard.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAttackTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not mill when the attack trigger is declined")
    void declinesAttackTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shard = addShard(player1);
        shard.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAttackTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger when it is not attached")
    void doesNotTriggerWhenUnattached() {
        addCreatureReady(player1, new GrizzlyBears());
        addShard(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Shard of Broken Glass"));
    }

    private Permanent addShard(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ShardOfBrokenGlass());
    }

    private void resolveAttackTrigger() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
