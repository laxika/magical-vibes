package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HypnoticSirenTest extends BaseCardTest {

    @Test
    @DisplayName("Hypnotic Siren can be cast normally as a flying creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new HypnoticSiren()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent siren = findPermanent(player1, "Hypnotic Siren");
        assertThat(gqs.isCreature(gd, siren)).isTrue();
        assertThat(gqs.hasKeyword(gd, siren, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Bestowed Hypnotic Siren steals, boosts, and grants flying to the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HypnoticSiren()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Hypnotic Siren becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HypnoticSiren()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent siren = findPermanent(player1, "Hypnotic Siren");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(siren);
        assertThat(gqs.isCreature(gd, siren)).isTrue();
        assertThat(siren.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Bestow cannot target a noncreature permanent")
    void cannotBestowToNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new HypnoticSiren()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
