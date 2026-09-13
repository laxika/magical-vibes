package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.p.PendrellDrake;
import com.github.laxika.magicalvibes.cards.r.Rescind;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({HiddenSpider.class, ArgothianSwine.class, PendrellDrake.class, Rescind.class})
class HiddenSpiderTest extends BaseCardTest {

    private Permanent addHiddenSpider() {
        return harness.addToBattlefieldAndReturn(player1, new HiddenSpider());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("An opponent's flying creature spell makes Hidden Spider a 3/5 Spider creature with reach")
    void becomesSpiderCreatureWhenOpponentCastsFlyingCreature() {
        Permanent spider = addHiddenSpider();
        prepareOpponentCast();

        harness.castFromHand(player2, new PendrellDrake(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, spider)).isTrue();
        assertThat(gqs.isEnchantment(gd, spider)).isFalse();
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, spider)).containsExactly(CardSubtype.SPIDER);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("A nonflying creature spell does not trigger Hidden Spider")
    void doesNotTriggerForNonflyingCreature() {
        Permanent spider = addHiddenSpider();
        prepareOpponentCast();

        harness.castFromHand(player2, new ArgothianSwine(), "{3}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, spider)).isTrue();
        assertThat(gqs.isCreature(gd, spider)).isFalse();
    }

    @Test
    @DisplayName("The trigger does not fire after Hidden Spider has become a creature")
    void doesNotTriggerWhenAlreadyCreature() {
        Permanent spider = addHiddenSpider();
        prepareOpponentCast();

        harness.castFromHand(player2, new PendrellDrake(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromHand(player2, new PendrellDrake(), "{3}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, spider)).isTrue();
        assertThat(gqs.isEnchantment(gd, spider)).isFalse();
    }

    @Test
    @DisplayName("The ability does not trigger when its controller casts a flying creature spell")
    void doesNotTriggerForControllerFlyingCreature() {
        Permanent spider = addHiddenSpider();

        harness.castFromHand(player1, new PendrellDrake(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, spider)).isTrue();
        assertThat(gqs.isCreature(gd, spider)).isFalse();
    }

    @Test
    @DisplayName("A queued trigger does nothing if Hidden Spider leaves before it resolves")
    void checksEnchantmentConditionAgainAtResolution() {
        Permanent spider = addHiddenSpider();
        prepareOpponentCast();

        harness.castFromHand(player2, new PendrellDrake(), "{3}{U}");
        harness.setHand(player1, List.of(new Rescind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, spider.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Hidden Spider");
        harness.assertInHand(player1, "Hidden Spider");
    }
}
