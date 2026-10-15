package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NyxbornWolf.class, GrizzlyBears.class})
class NyxbornWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Nyxborn Wolf can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new NyxbornWolf()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, wolf)).isTrue();
    }

    @Test
    @DisplayName("Nyxborn Wolf can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NyxbornWolf()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("A bestowed Nyxborn Wolf becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NyxbornWolf()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent wolf = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Nyxborn Wolf"));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wolf);
        assertThat(gqs.isCreature(gd, wolf)).isTrue();
        assertThat(wolf.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Bestow can enchant an opponent's creature without making the Wolf a creature")
    void bestowsOnOpponentsCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new NyxbornWolf());
        harness.setHand(player1, List.of(new NyxbornWolf()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent aura = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Nyxborn Wolf"));
        assertThat(aura.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, aura)).isFalse();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bestow resolves as a creature if its target leaves before resolution")
    void resolvesAsCreatureWhenTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new NyxbornWolf());
        harness.setHand(player1, List.of(new NyxbornWolf()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent wolf = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Nyxborn Wolf"));
        assertThat(gqs.isCreature(gd, wolf)).isTrue();
        assertThat(wolf.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Nyxborn Wolf");
    }
}
