package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BurnishedHart;
import com.github.laxika.magicalvibes.cards.c.CommuneWithTheGods;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nighthowler.class, BurnishedHart.class, CommuneWithTheGods.class})
class NighthowlerTest extends BaseCardTest {

    @Test
    @DisplayName("Nighthowler gets +X/+X for creature cards in all graveyards when cast normally")
    void boostsItselfFromAllGraveyards() {
        harness.setGraveyard(player1, List.of(new BurnishedHart()));
        harness.setGraveyard(player2, List.of(new BurnishedHart(), new BurnishedHart()));

        harness.castFromHand(player1, new Nighthowler(), "{1}{B}{B}");
        harness.passBothPriorities();

        Permanent nighthowler = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, nighthowler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nighthowler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bestowed Nighthowler boosts the enchanted creature from all graveyards")
    void boostsEnchantedCreatureFromAllGraveyards() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());
        harness.setGraveyard(player1, List.of(new BurnishedHart()));
        harness.setGraveyard(player2, List.of(new BurnishedHart(), new BurnishedHart()));

        harness.setHand(player1, List.of(new Nighthowler()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);

        gd.playerGraveyards.get(player1.getId()).add(new BurnishedHart());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(6);
    }

    @Test
    @DisplayName("Bestowed Nighthowler becomes a creature with the same boost when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());
        harness.setGraveyard(player1, List.of(new BurnishedHart()));
        harness.setGraveyard(player2, List.of(new BurnishedHart()));

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

    @Test
    @DisplayName("Nighthowler dies when no creature cards are in graveyards")
    void diesWithNoCreatureCardsInGraveyards() {
        harness.setGraveyard(player1, List.of(new CommuneWithTheGods()));
        harness.castFromHand(player1, new Nighthowler(), "{1}{B}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nighthowler");
        harness.assertInGraveyard(player1, "Nighthowler");
    }

    @Test
    @DisplayName("Bestow remains an Aura with empty graveyards and counts only creatures as graveyards change")
    void bestowedBoostTracksOnlyCreatureCards() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        harness.setGraveyard(player1, List.of(new CommuneWithTheGods()));
        harness.setHand(player1, List.of(new Nighthowler()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent nighthowler = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, nighthowler)).isFalse();
        assertThat(nighthowler.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);

        harness.setGraveyard(player2, List.of(new BurnishedHart(), new Nighthowler()));
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(4);

        harness.setGraveyard(player2, List.of());
        harness.runStateBasedActions();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
        assertThat(nighthowler.getAttachedTo()).isEqualTo(host.getId());
        harness.assertOnBattlefield(player1, "Nighthowler");
    }

    @Test
    @DisplayName("Bestow resolves as a creature when its target leaves before resolution")
    void resolvesAsCreatureWhenBestowTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());
        harness.setHand(player1, List.of(new Nighthowler()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.passBothPriorities();

        Permanent nighthowler = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, nighthowler)).isTrue();
        assertThat(nighthowler.isAttached()).isFalse();
        assertThat(gqs.getEffectivePower(gd, nighthowler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nighthowler)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature Nighthowler dies after the last creature card leaves a graveyard")
    void creatureDiesWhenGraveyardCountDropsToZero() {
        harness.setGraveyard(player2, List.of(new BurnishedHart()));
        harness.castFromHand(player1, new Nighthowler(), "{1}{B}{B}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Nighthowler");

        harness.setGraveyard(player2, List.of());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Nighthowler");
        harness.assertInGraveyard(player1, "Nighthowler");
    }
}
