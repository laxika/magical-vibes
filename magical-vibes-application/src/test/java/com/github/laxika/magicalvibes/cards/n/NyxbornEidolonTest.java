package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FelhideBrawler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NyxbornEidolon.class, FelhideBrawler.class})
class NyxbornEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Nyxborn Eidolon can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new NyxbornEidolon()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
    }

    @Test
    @DisplayName("Nyxborn Eidolon can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new NyxbornEidolon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("A bestowed Nyxborn Eidolon becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new NyxbornEidolon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent eidolon = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Nyxborn Eidolon"));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(eidolon);
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(eidolon.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Bestow resolves as a creature if its target leaves before resolution")
    void resolvesAsCreatureWhenTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new NyxbornEidolon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(eidolon.getCard()).isInstanceOf(NyxbornEidolon.class);
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(eidolon.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Nyxborn Eidolon");
    }

    @Test
    @DisplayName("Bestow can enchant an opponent's creature without changing its controller")
    void bestowsOpponentsCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new FelhideBrawler());
        harness.setHand(player1, List.of(new NyxbornEidolon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(eidolon.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, eidolon)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(host);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bestow requires its full alternative cost even when the normal cost is affordable")
    void rejectsInsufficientBestowMana() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new NyxbornEidolon()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, host.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Nyxborn Eidolon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The enchanted creature loses the boost when the bestowed Eidolon leaves")
    void boostEndsWhenEidolonLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new NyxbornEidolon()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent eidolon = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Nyxborn Eidolon"));
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, eidolon));
        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Nyxborn Eidolon");
    }
}
