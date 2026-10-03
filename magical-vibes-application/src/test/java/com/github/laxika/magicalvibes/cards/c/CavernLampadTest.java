package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CavernLampad.class, TravelingPhilosopher.class})
class CavernLampadTest extends BaseCardTest {

    @Test
    @DisplayName("Cavern Lampad can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new CavernLampad()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent lampad = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, lampad)).isTrue();
    }

    @Test
    @DisplayName("Cavern Lampad can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CavernLampad()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent lampad = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Cavern Lampad"));
        assertThat(gqs.isCreature(gd, lampad)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Cavern Lampad becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CavernLampad()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent lampad = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Cavern Lampad"));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lampad);
        assertThat(gqs.isCreature(gd, lampad)).isTrue();
        assertThat(lampad.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Intimidate uses the enchanted creature's color rather than Lampad's color")
    void bestowedIntimidateUsesHostsColor() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent whiteBlocker = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        Permanent blackBlocker = harness.addToBattlefieldAndReturn(player2, new CavernLampad());
        harness.setHand(player1, List.of(new CavernLampad()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        List<Permanent> defenders = gd.playerBattlefields.get(player2.getId());
        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, whiteBlocker, host, defenders)).isTrue();
        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, blackBlocker, host, defenders)).isFalse();
    }

    @Test
    @DisplayName("Bestow resolves as a creature when its target leaves before resolution")
    void resolvesAsCreatureWhenTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CavernLampad()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent lampad = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Cavern Lampad"));
        assertThat(gqs.isCreature(gd, lampad)).isTrue();
        assertThat(lampad.isAttached()).isFalse();
        assertThat(gqs.getEffectivePower(gd, lampad)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lampad)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lampad, Keyword.INTIMIDATE)).isTrue();
        harness.assertNotInGraveyard(player1, "Cavern Lampad");
    }

    @Test
    @DisplayName("Bestow boosts an opponent's creature and the bonus ends when Lampad leaves")
    void enchantsOpponentsCreatureAndBonusEndsWhenRemoved() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new CavernLampad()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent lampad = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Cavern Lampad"));
        assertThat(lampad.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, lampad)).isFalse();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, host, Keyword.INTIMIDATE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lampad));
        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, host, Keyword.INTIMIDATE)).isFalse();
        harness.assertInGraveyard(player1, "Cavern Lampad");
    }
}
