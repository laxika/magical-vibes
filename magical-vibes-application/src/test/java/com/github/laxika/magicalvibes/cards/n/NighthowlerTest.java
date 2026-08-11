package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NighthowlerTest extends BaseCardTest {

    @Test
    @DisplayName("Nighthowler gets +X/+X for creature cards in all graveyards when cast normally")
    void boostsItselfFromAllGraveyards() {
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new Nighthowler()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent nighthowler = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, nighthowler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nighthowler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bestowed Nighthowler boosts the enchanted creature from all graveyards")
    void boostsEnchantedCreatureFromAllGraveyards() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new Nighthowler()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);

        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(6);
    }

    @Test
    @DisplayName("Bestowed Nighthowler becomes a creature with the same boost when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new Nighthowler()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent nighthowler = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gqs.isCreature(gd, nighthowler)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nighthowler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nighthowler)).isEqualTo(3);
        assertThat(nighthowler.isAttached()).isFalse();
    }
}
