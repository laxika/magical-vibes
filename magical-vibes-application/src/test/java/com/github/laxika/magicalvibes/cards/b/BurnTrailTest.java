package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BurnTrailTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a target player")
    void dealsThreeDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BurnTrail()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 3 damage to a target creature, destroying it")
    void dealsThreeDamageToCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurnTrail()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Conspire taps two red creatures and queues a copy of the spell")
    void conspireTapsCreaturesAndQueuesCopy() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BurnTrail()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent goblin1 = addCreatureReady(player1, new RagingGoblin());
        Permanent goblin2 = addCreatureReady(player1, new RagingGoblin());

        harness.castWithConspire(player1, 0, player2.getId(), List.of(goblin1.getId(), goblin2.getId()));

        assertThat(goblin1.isTapped()).isTrue();
        assertThat(goblin2.isTapped()).isTrue();

        // The spell plus one conspire copy trigger are on the stack.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack).anyMatch(e -> e.getEffectsToResolve().stream()
                .anyMatch(fx -> fx instanceof CopyControllerCastSpellEffect));
    }
}
