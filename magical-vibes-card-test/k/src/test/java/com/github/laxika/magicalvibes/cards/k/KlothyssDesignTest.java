package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AltarOfThePantheon;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheHunt;
import com.github.laxika.magicalvibes.cards.t.ThryxTheSuddenStorm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KlothyssDesign.class, GrizzlyBears.class, AltarOfThePantheon.class,
        NyxbornColossus.class, OmenOfTheHunt.class, ThryxTheSuddenStorm.class})
class KlothyssDesignTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +X/+X equal to your green devotion")
    void boostsOwnCreaturesByGreenDevotion() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new KlothyssDesign(), "{5}{G}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The devotion-based boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new KlothyssDesign(), "{5}{G}");
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void devotionIsDeterminedAtResolutionAndDoesNotChangeAfterward() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        harness.castFromHand(player1, new KlothyssDesign(), "{5}{G}");
        Permanent second = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(13);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(13);

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, lateCreature)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(12);
        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerGraveyards.get(player1.getId()).add(second.getCard());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(13);
    }

    @Test
    void countsDevotionBonusFromNoncreaturePermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        harness.addToBattlefield(player1, new AltarOfThePantheon());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new OmenOfTheHunt());
        harness.addToBattlefield(player2, new NyxbornColossus());
        harness.addToBattlefield(player2, new AltarOfThePantheon());
        harness.castFromHand(player1, new KlothyssDesign(), "{5}{G}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(12);
        assertThat(enchantment.getPowerModifier()).isZero();
        assertThat(enchantment.getToughnessModifier()).isZero();
    }

    @Test
    void devotionLostBeforeResolutionDoesNotContribute() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new OmenOfTheHunt());
        harness.castFromHand(player1, new KlothyssDesign(), "{5}{G}");
        gd.playerBattlefields.get(player1.getId()).remove(enchantment);
        gd.playerGraveyards.get(player1.getId()).add(enchantment.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
    }

    @Test
    void zeroDevotionDoesNotCountSpellHandGraveyardOrOpposingPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ThryxTheSuddenStorm());
        harness.addToBattlefield(player2, new NyxbornColossus());
        harness.setGraveyard(player1, List.of(new NyxbornColossus()));
        harness.castFromHand(player1, new KlothyssDesign(), "{5}{G}");
        harness.setHand(player1, List.of(new NyxbornColossus()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }
}
