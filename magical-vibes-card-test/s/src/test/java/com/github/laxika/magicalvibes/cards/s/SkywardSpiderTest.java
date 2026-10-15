package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DivineFavor;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.w.WebShooters;
import com.github.laxika.magicalvibes.cards.w.WebUp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkywardSpider.class, GiantGrowth.class, Shock.class, DivineFavor.class,
        WebShooters.class, WebUp.class})
class SkywardSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying only while modified")
    void hasFlyingOnlyWhileModified() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new SkywardSpider());

        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isFalse();

        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isTrue();

        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay {2}")
    void wardCountersUnpaidSpell() {
        Permanent spider = addSpider();
        prepareOpponentCast();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, spider.getId());

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Skyward Spider");
    }

    @Test
    @DisplayName("Paying {2} lets an opponent's spell targeting it resolve")
    void payingWardManaLetsSpellResolve() {
        Permanent spider = addSpider();
        prepareOpponentCast();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, spider.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(5);
    }

    @Test
    @DisplayName("A counter other than +1/+1 also grants flying")
    void otherCounterGrantsFlying() {
        Permanent spider = addSpider();
        spider.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equipment grants flying regardless of its controller, until detached")
    void equipmentGrantsFlyingRegardlessOfController() {
        Permanent spider = addSpider();
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new WebShooters());
        equipment.setAttachedTo(spider.getId());

        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isTrue();

        equipment.setAttachedTo(null);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An Aura controlled by the Spider's controller grants flying until detached")
    void friendlyAuraGrantsFlyingUntilDetached() {
        Permanent spider = addSpider();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DivineFavor());
        aura.setAttachedTo(spider.getId());

        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isTrue();

        aura.setAttachedTo(null);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Aura does not grant flying")
    void opposingAuraDoesNotGrantFlying() {
        Permanent spider = addSpider();
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new DivineFavor());
        aura.setAttachedTo(spider.getId());

        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A friendly pump spell needs no ward payment and does not modify the Spider")
    void friendlyPumpSpellDoesNotTriggerWardOrGrantFlying() {
        Permanent spider = addSpider();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, spider.getId());

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent can decline ward even with enough mana")
    void decliningWardCountersSpell() {
        Permanent spider = addSpider();
        prepareOpponentCast();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, spider.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ward counters an opponent's triggered ability without removing its source")
    void wardCountersOpposingTriggeredAbility() {
        addSpider();
        prepareOpponentCast();
        harness.setHand(player2, List.of(new WebUp()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castEnchantment(player2, 0, harness.getPermanentId(player1, "Skyward Spider"));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyward Spider");
        harness.assertOnBattlefield(player2, "Web Up");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addSpider() {
        return harness.addToBattlefieldAndReturn(player1, new SkywardSpider());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
