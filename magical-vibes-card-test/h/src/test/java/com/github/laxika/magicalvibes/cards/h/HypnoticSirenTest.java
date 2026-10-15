package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ChariotOfVictory;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
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

@CardUsed({HypnoticSiren.class, GoldenHind.class, ChariotOfVictory.class})
class HypnoticSirenTest extends BaseCardTest {

    @Test
    @DisplayName("Hypnotic Siren can be cast normally as a flying creature")
    void castsNormallyAsCreature() {
        harness.castFromHand(player1, new HypnoticSiren(), "{U}");
        harness.passBothPriorities();

        Permanent siren = findPermanent(player1, "Hypnotic Siren");
        assertThat(gqs.isCreature(gd, siren)).isTrue();
        assertThat(gqs.hasKeyword(gd, siren, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Bestowed Hypnotic Siren steals, boosts, and grants flying to the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        harness.setHand(player1, List.of(new HypnoticSiren()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
        Permanent siren = findPermanent(player1, "Hypnotic Siren");
        assertThat(gqs.isCreature(gd, siren)).isFalse();
        assertThat(siren.getAttachedTo()).isEqualTo(bear.getId());
    }

    @Test
    @DisplayName("A bestowed Hypnotic Siren becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ChariotOfVictory());
        harness.setHand(player1, List.of(new HypnoticSiren()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Bestow resolves as a creature if its target leaves before resolution")
    void resolvesAsCreatureWhenTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        harness.setHand(player1, List.of(new HypnoticSiren()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent siren = findPermanent(player1, "Hypnotic Siren");
        assertThat(gqs.isCreature(gd, siren)).isTrue();
        assertThat(siren.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Hypnotic Siren");
    }

    @Test
    @DisplayName("Removing the bestowed Siren returns control and removes its grants")
    void removingSirenEndsControlAndGrants() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        harness.setHand(player1, List.of(new HypnoticSiren()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent siren = findPermanent(player1, "Hypnotic Siren");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, siren));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(host);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(host);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, host, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Bestow requires two blue mana even with enough total mana")
    void bestowRequiresTwoBlueMana() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        harness.setHand(player1, List.of(new HypnoticSiren()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, host.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Hypnotic Siren");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(host);
    }
}
