package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({NobleQuarry.class, GrizzlyBears.class})
class NobleQuarryTest extends BaseCardTest {

    @Test
    @DisplayName("Noble Quarry can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new NobleQuarry()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent quarry = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, quarry)).isTrue();
    }

    @Test
    @DisplayName("Noble Quarry can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NobleQuarry()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        Permanent quarry = findPermanent(player1, "Noble Quarry");
        assertThat(quarry.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.isCreature(gd, quarry)).isFalse();
    }

    @Test
    @DisplayName("All able creatures must block a creature enchanted by Noble Quarry")
    void allAbleCreaturesMustBlockEnchantedCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NobleQuarry()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castWithAlternateCost(player1, 0, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    void creatureFormRequiresAllUntappedBlockers() {
        Permanent quarry = addCreatureReady(player1, new NobleQuarry());
        quarry.setAttacking(true);
        Permanent first = addCreatureReady(player2, new NobleQuarry());
        Permanent second = addCreatureReady(player2, new NobleQuarry());
        Permanent tapped = addCreatureReady(player2, new NobleQuarry());
        tapped.tap();
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    void becomesCreatureWhenBestowTargetLeavesBeforeResolution() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new NobleQuarry());
        harness.setHand(player1, List.of(new NobleQuarry()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castWithAlternateCost(player1, 0, host.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.passBothPriorities();

        Permanent quarry = findPermanent(player1, "Noble Quarry");
        assertThat(gqs.isCreature(gd, quarry)).isTrue();
        assertThat(quarry.isAttached()).isFalse();
    }

    @Test
    void detachedBestowCreatureStillRequiresAllBlockers() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new NobleQuarry());
        harness.setHand(player1, List.of(new NobleQuarry()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent quarry = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != host).findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(quarry);
        assertThat(gqs.isCreature(gd, quarry)).isTrue();
        assertThat(quarry.isAttached()).isFalse();
        quarry.setSummoningSick(false);
        quarry.setAttacking(true);
        addCreatureReady(player2, new NobleQuarry());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
