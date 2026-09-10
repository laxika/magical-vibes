package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShardmagesRescue.class, GrizzlyBears.class})
class ShardmagesRescueTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and hexproof when Shardmage's Rescue enters")
    void grantsBoostAndHexproofWhenItEnters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castRescue(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Hexproof lasts only through the turn Shardmage's Rescue enters")
    void hexproofWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castRescue(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shardmage's Rescue can enchant only a creature you control")
    void cannotEnchantOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShardmagesRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID opponentCreature = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRescue(Permanent creature) {
        harness.setHand(player1, List.of(new ShardmagesRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
