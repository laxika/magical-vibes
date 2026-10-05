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

@CardUsed({NyxbornShieldmate.class, GrizzlyBears.class})
class NyxbornShieldmateTest extends BaseCardTest {

    @Test
    @DisplayName("Nyxborn Shieldmate can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new NyxbornShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent shieldmate = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, shieldmate)).isTrue();
    }

    @Test
    @DisplayName("Nyxborn Shieldmate can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NyxbornShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("A bestowed Nyxborn Shieldmate becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NyxbornShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent shieldmate = findPermanent(player1, "Nyxborn Shieldmate");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shieldmate);
        assertThat(gqs.isCreature(gd, shieldmate)).isTrue();
        assertThat(shieldmate.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Bestow can enchant an opponent's creature without becoming a creature itself")
    void bestowsOntoOpponentsCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new NyxbornShieldmate());
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new NyxbornShieldmate());
        harness.setHand(player1, List.of(new NyxbornShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isAttached).findFirst().orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, aura)).isFalse();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bystander)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bystander)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bestow resolves as a creature if its target leaves before resolution")
    void resolvesAsCreatureWhenBestowTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new NyxbornShieldmate());
        harness.setHand(player1, List.of(new NyxbornShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent shieldmate = findPermanent(player1, "Nyxborn Shieldmate");
        assertThat(gqs.isCreature(gd, shieldmate)).isTrue();
        assertThat(shieldmate.isAttached()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
