package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castCreature(player2, 0);
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

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, spider)).isTrue();
        assertThat(gqs.isCreature(gd, spider)).isFalse();
    }

    @Test
    @DisplayName("The trigger does not fire after Hidden Spider has become a creature")
    void doesNotTriggerWhenAlreadyCreature() {
        Permanent spider = addHiddenSpider();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, spider)).isTrue();
        assertThat(gqs.isEnchantment(gd, spider)).isFalse();
    }
}
