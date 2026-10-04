package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FelhideBrawler;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldOfTorment.class, FelhideBrawler.class})
class HeraldOfTormentTest extends BaseCardTest {

    @Test
    @DisplayName("Herald of Torment can be cast normally and causes its controller to lose life at upkeep")
    void castsNormallyAndTriggersAtUpkeep() {
        harness.addToBattlefield(player1, new HeraldOfTorment());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent herald = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, herald)).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Herald of Torment bestowed to a creature grants +3/+3 and flying")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new HeraldOfTorment()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent herald = findPermanent(player1, "Herald of Torment");
        assertThat(gqs.isCreature(gd, herald)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Herald of Torment becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new HeraldOfTorment()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent herald = findPermanent(player1, "Herald of Torment");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(herald);
        assertThat(gqs.isCreature(gd, herald)).isTrue();
        assertThat(herald.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Casting Herald of Torment normally needs no target")
    void normalCastResolvesWithoutTarget() {
        harness.castFromHand(player1, new HeraldOfTorment(), "{1}{B}{B}");
        harness.passBothPriorities();

        Permanent herald = findPermanent(player1, "Herald of Torment");
        assertThat(gqs.isCreature(gd, herald)).isTrue();
        assertThat(herald.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Herald of Torment");
    }

    @Test
    @DisplayName("Bestow resolves as a creature when its target leaves before resolution")
    void bestowResolvesAsCreatureWithMissingTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new HeraldOfTorment()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castWithAlternateCost(player1, 0, bear.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent herald = findPermanent(player1, "Herald of Torment");
        assertThat(gqs.isCreature(gd, herald)).isTrue();
        assertThat(herald.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Herald of Torment");
    }

    @Test
    @DisplayName("Bestowed Herald makes its own controller lose life on their upkeep")
    void bestowedHeraldTriggersForAuraController() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new FelhideBrawler());
        harness.setHand(player1, List.of(new HeraldOfTorment()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(findPermanent(player1, "Herald of Torment").getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
    }

    @Test
    @DisplayName("Bestowed Herald does not trigger on the enchanted creature controller's upkeep")
    void bestowedHeraldDoesNotTriggerOnOpponentsUpkeep() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new FelhideBrawler());
        harness.setHand(player1, List.of(new HeraldOfTorment()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
