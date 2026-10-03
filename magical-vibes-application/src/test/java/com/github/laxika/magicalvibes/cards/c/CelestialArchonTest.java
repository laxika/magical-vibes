package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RayOfDissolution;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CelestialArchon.class, TravelingPhilosopher.class, RayOfDissolution.class})
class CelestialArchonTest extends BaseCardTest {

    @Test
    @DisplayName("Celestial Archon can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new CelestialArchon()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent archon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, archon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, archon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, archon)).isEqualTo(4);
    }

    @Test
    @DisplayName("Celestial Archon can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CelestialArchon()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent archon = findPermanent(player1, "Celestial Archon");
        assertThat(gqs.isCreature(gd, archon)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Celestial Archon becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CelestialArchon()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent archon = findPermanent(player1, "Celestial Archon");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(archon);
        assertThat(gqs.isCreature(gd, archon)).isTrue();
        assertThat(archon.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Bestow resolves as a creature if its target leaves before resolution")
    void resolvesAsCreatureWhenBestowTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CelestialArchon()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent archon = findPermanent(player1, "Celestial Archon");
        assertThat(gqs.isCreature(gd, archon)).isTrue();
        assertThat(archon.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Celestial Archon");
    }

    @Test
    @DisplayName("Bestow can enchant an opponent's creature without changing its controller")
    void bestowsOnOpponentsCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CelestialArchon()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent archon = findPermanent(player1, "Celestial Archon");
        assertThat(archon.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, archon)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(host);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, host, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, host, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Destroying the bestowed Aura removes its bonus and puts Archon in the graveyard")
    void destroyingAuraEndsItsBonus() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CelestialArchon()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent archon = findPermanent(player1, "Celestial Archon");

        harness.setHand(player2, List.of(new RayOfDissolution()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0, archon.getId());

        harness.assertInGraveyard(player1, "Celestial Archon");
        harness.assertNotOnBattlefield(player1, "Celestial Archon");
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, host, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, host, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The normal casting cost is insufficient to pay for bestow")
    void cannotBestowForNormalCost() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CelestialArchon()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, host.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Celestial Archon");
        assertThat(gd.stack).isEmpty();
    }
}
