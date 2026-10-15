package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FelhideMinotaur;
import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.w.WingsOfVelisVel;
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

@CardUsed({KragmaWarcaller.class, FelhideMinotaur.class, BronzeSable.class, WingsOfVelisVel.class})
class KragmaWarcallerTest extends BaseCardTest {

    @Test
    @DisplayName("Minotaurs you control have haste")
    void grantsHasteToOwnMinotaurs() {
        addCreatureReady(player1, new KragmaWarcaller());
        Permanent minotaur = addCreatureReady(player1, new FelhideMinotaur());

        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Kragma Warcaller does not grant haste to non-Minotaurs or opposing Minotaurs")
    void hasteGrantIsLimitedToOwnMinotaurs() {
        addCreatureReady(player1, new KragmaWarcaller());
        Permanent nonMinotaur = addCreatureReady(player1, new BronzeSable());
        Permanent opposingMinotaur = addCreatureReady(player2, new FelhideMinotaur());

        assertThat(gqs.hasKeyword(gd, nonMinotaur, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingMinotaur, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A Minotaur that attacks gets +2/+0 until end of turn")
    void boostsAttackingMinotaur() {
        addCreatureReady(player1, new KragmaWarcaller());
        Permanent minotaur = addCreatureReady(player1, new FelhideMinotaur());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(minotaur.getPowerModifier()).isEqualTo(2);
        assertThat(minotaur.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A non-Minotaur attacker does not get the attack bonus")
    void doesNotBoostNonMinotaurAttacker() {
        addCreatureReady(player1, new KragmaWarcaller());
        Permanent nonMinotaur = addCreatureReady(player1, new BronzeSable());

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(nonMinotaur.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Warcaller grants itself haste and boosts itself when it attacks")
    void canAttackImmediatelyAndBoostsItself() {
        harness.castFromHand(player1, new KragmaWarcaller(), "{3}{B}{R}");
        harness.passBothPriorities();
        Permanent warcaller = findPermanent(player1, "Kragma Warcaller");

        assertThat(gqs.hasKeyword(gd, warcaller, Keyword.HASTE)).isTrue();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(warcaller.getPowerModifier()).isEqualTo(2);
        assertThat(warcaller.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each attacking Minotaur gets its own bonus while nonattackers get none")
    void boostsEachAttackerIndependently() {
        Permanent warcaller = addCreatureReady(player1, new KragmaWarcaller());
        Permanent first = addCreatureReady(player1, new FelhideMinotaur());
        Permanent second = addCreatureReady(player1, new FelhideMinotaur());
        Permanent nonattacker = addCreatureReady(player1, new FelhideMinotaur());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(warcaller.getPowerModifier()).isEqualTo(2);
        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(nonattacker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Multiple Warcallers each boost an attacking Minotaur")
    void bonusesFromMultipleWarcallersAccumulate() {
        addCreatureReady(player1, new KragmaWarcaller());
        addCreatureReady(player1, new KragmaWarcaller());
        Permanent minotaur = addCreatureReady(player1, new FelhideMinotaur());

        declareAttackers(player1, List.of(2));
        resolveAllTriggers();

        assertThat(minotaur.getPowerModifier()).isEqualTo(4);
        assertThat(minotaur.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Opposing Minotaur attacks do not trigger Warcaller")
    void doesNotBoostOpposingMinotaur() {
        addCreatureReady(player1, new KragmaWarcaller());
        Permanent minotaur = addCreatureReady(player2, new FelhideMinotaur());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(minotaur.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The attack bonus expires at the end of the turn")
    void attackBonusExpires() {
        addCreatureReady(player1, new KragmaWarcaller());
        Permanent minotaur = addCreatureReady(player1, new FelhideMinotaur());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        assertThat(minotaur.getPowerModifier()).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(minotaur.getPowerModifier()).isZero();
        assertThat(minotaur.getToughnessModifier()).isZero();
    }

    @Test
    @CardUsed({KragmaWarcaller.class, BronzeSable.class, WingsOfVelisVel.class})
    @DisplayName("A creature that gains the Minotaur type receives the attack bonus")
    void boostsCreatureThatGainedMinotaurType() {
        addCreatureReady(player1, new KragmaWarcaller());
        Permanent sable = addCreatureReady(player1, new BronzeSable());
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, sable.getId());

        assertThat(gqs.hasKeyword(gd, sable, Keyword.HASTE)).isTrue();
        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, sable)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sable)).isEqualTo(4);
    }
}
