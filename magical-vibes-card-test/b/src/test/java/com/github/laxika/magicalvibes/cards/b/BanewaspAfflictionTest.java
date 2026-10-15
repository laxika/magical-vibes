package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BanewaspAffliction.class, GiantSpider.class, GrizzlyBears.class, FountainOfYouth.class})
class BanewaspAfflictionTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature dies, its controller loses life equal to its toughness")
    void enchantedCreatureDeathLosesLifeEqualToToughness() {
        // Giant Spider is 2/4 — this proves the loss tracks toughness (4), not power (2).
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent banewasp = harness.addToBattlefieldAndReturn(player1, new BanewaspAffliction());
        banewasp.setAttachedTo(spider.getId());

        int lifeBefore = gd.getLife(player2.getId());

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("The aura's own controller loses the life when it enchants their creature")
    void ownControllerLosesLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent banewasp = harness.addToBattlefieldAndReturn(player1, new BanewaspAffliction());
        banewasp.setAttachedTo(bears.getId());

        int lifeBefore = gd.getLife(player1.getId());

        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new BanewaspAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void resolvedAuraUsesToughnessWithCountersAtDeath() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new BanewaspAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, spider.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Banewasp Affliction").getAttachedTo()).isEqualTo(spider.getId());
        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        spider.setMarkedDamage(6);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertInGraveyard(player1, "Banewasp Affliction");
    }

    @Test
    void negativeToughnessDeathDoesNotGainLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BanewaspAffliction());
        aura.setAttachedTo(bears.getId());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    void exilingEnchantedCreatureDoesNotCauseLifeLoss() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BanewaspAffliction());
        aura.setAttachedTo(spider.getId());

        harness.getPermanentRemovalService().removePermanentToExile(gd, spider);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertNotInGraveyard(player2, "Giant Spider");
        harness.assertInGraveyard(player1, "Banewasp Affliction");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }
}
