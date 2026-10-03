package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EatenByPiranhas;
import com.github.laxika.magicalvibes.cards.r.RiverHeraldScout;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BedrockTortoise.class, GiantSpider.class, GoblinPiker.class, GrizzlyBears.class,
        EatenByPiranhas.class, RiverHeraldScout.class})
class BedrockTortoiseTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control with greater toughness assign combat damage equal to toughness")
    void higherToughnessCreaturesUseToughnessForCombatDamage() {
        addCreatureReady(player1, new BedrockTortoise());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent piker = addCreatureReady(player1, new GoblinPiker());

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4);
        assertThat(gqs.getEffectiveCombatDamage(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Tortoise itself and only your creatures use the restricted damage effect")
    void sourceAndControllerScopeAreCorrect() {
        Permanent tortoise = addCreatureReady(player1, new BedrockTortoise());
        Permanent opponentSpider = addCreatureReady(player2, new GiantSpider());

        assertThat(gqs.getEffectiveCombatDamage(gd, tortoise)).isEqualTo(6);
        assertThat(gqs.getEffectiveCombatDamage(gd, opponentSpider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures you control have hexproof only during your turn")
    void grantsHexproofDuringControllerTurn() {
        Permanent tortoise = addCreatureReady(player1, new BedrockTortoise());
        Permanent spider = addCreatureReady(player1, new GiantSpider());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, tortoise, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.HEXPROOF)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, tortoise, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void tortoiseDealsSixCombatDamageWithoutChangingItsPower() {
        Permanent tortoise = addCreatureReady(player1, new BedrockTortoise());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gqs.getPowerBasedDamage(gd, tortoise)).isZero();
    }

    @Test
    void toughnessAssignmentAlsoAppliesDuringOpponentsTurn() {
        addCreatureReady(player1, new BedrockTortoise());
        Permanent scout = addCreatureReady(player1, new RiverHeraldScout());
        Permanent opponentScout = addCreatureReady(player2, new RiverHeraldScout());
        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectiveCombatDamage(gd, scout)).isEqualTo(2);
        assertThat(gqs.getEffectiveCombatDamage(gd, opponentScout)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentScout, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void losingAbilitiesStopsToughnessAssignmentForOtherCreatures() {
        Permanent tortoise = addCreatureReady(player1, new BedrockTortoise());
        Permanent scout = addCreatureReady(player1, new RiverHeraldScout());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new EatenByPiranhas()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castEnchantment(player2, 0, tortoise.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectiveCombatDamage(gd, scout)).isEqualTo(1);
    }

    @Test
    void removingTortoiseEndsBothStaticEffects() {
        Permanent tortoise = addCreatureReady(player1, new BedrockTortoise());
        Permanent scout = addCreatureReady(player1, new RiverHeraldScout());
        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, scout, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.getEffectiveCombatDamage(gd, scout)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(tortoise);

        assertThat(gqs.hasKeyword(gd, scout, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectiveCombatDamage(gd, scout)).isEqualTo(1);
    }
}
