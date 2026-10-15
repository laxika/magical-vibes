package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LumithreadField;
import com.github.laxika.magicalvibes.cards.e.EmblemOfTheWarmind;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import java.util.List;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuragandaPetroglyphs.class, GrizzlyBears.class, GiantSpider.class, LlanowarElves.class, LumithreadField.class, EmblemOfTheWarmind.class, HolyStrength.class})
class MuragandaPetroglyphsTest extends BaseCardTest {

    @Test
    void boostsFaceDownMorphCreatureDespiteHiddenPrintedAbilities() {
        harness.addToBattlefield(player1, new MuragandaPetroglyphs());
        harness.setHand(player1, List.of(new LumithreadField()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent field = findPermanent(player1, "Lumithread Field");

        assertThat(field.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, field)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, field)).isEqualTo(4);
    }

    @Test
    void grantedHasteRemovesBonusOnlyFromAffectedCreatures() {
        harness.addToBattlefield(player1, new MuragandaPetroglyphs());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EmblemOfTheWarmind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, ownBears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(4);
    }

    @Test
    void auraThatOnlyBoostsPowerAndToughnessDoesNotRemoveBonus() {
        harness.addToBattlefield(player1, new MuragandaPetroglyphs());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    void multipleCopiesStackAndBonusEndsWhenSourceLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MuragandaPetroglyphs());
        harness.addToBattlefield(player1, new MuragandaPetroglyphs());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void boostsCreaturesWithNoAbilitiesOnEitherBattlefield() {
        harness.addToBattlefield(player1, new MuragandaPetroglyphs());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
    }

    @Test
    void doesNotBoostCreaturesWithKeywordAbilities() {
        harness.addToBattlefield(player1, new MuragandaPetroglyphs());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(4);
    }
}
