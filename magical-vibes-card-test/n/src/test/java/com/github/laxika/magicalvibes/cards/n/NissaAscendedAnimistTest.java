package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.l.LuxiorGiadasGift;
import com.github.laxika.magicalvibes.cards.o.Ossification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NissaAscendedAnimist.class, Forest.class, CopperLonglegs.class, PropheticPrism.class, Ossification.class, LuxiorGiadasGift.class})
class NissaAscendedAnimistTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with seven loyalty when both Phyrexian symbols are paid with mana")
    void entersWithFullLoyaltyWhenPaidWithMana() {
        harness.setHand(player1, List.of(new NissaAscendedAnimist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        Permanent nissa = findPermanent(player1, "Nissa, Ascended Animist");
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Compleated reduces entry loyalty by two for each Phyrexian symbol paid with life")
    void compleatedReducesLoyaltyForPhyrexianLife() {
        harness.setHand(player1, List.of(new NissaAscendedAnimist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        Permanent nissa = findPermanent(player1, "Nissa, Ascended Animist");
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("+1 creates a Horror whose size equals Nissa's loyalty after the loyalty cost")
    void plusOneCreatesLoyaltySizedHorror() {
        addReadyNissa(player1, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Phyrexian Horror");
        assertThat(token.getEffectivePower()).isEqualTo(6);
        assertThat(token.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("-1 destroys a target artifact")
    void minusOneDestroysArtifact() {
        addReadyNissa(player1, 5);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
    }

    @Test
    @DisplayName("-1 cannot target a creature")
    void minusOneCannotTargetCreature() {
        addReadyNissa(player1, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-7 boosts your creatures by the number of Forests and grants trample")
    void minusSevenBoostsOwnCreaturesByForests() {
        addReadyNissa(player1, 7);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
    }

    @Test
    void payingLifeForOneSymbolReducesLoyaltyByTwo() {
        harness.setHand(player1, List.of(new NissaAscendedAnimist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Nissa, Ascended Animist")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player1, 18);
    }

    @Test
    void horrorUsesResolutionLoyaltyAndKeepsItsSize() {
        Permanent nissa = addReadyNissa(player1, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        nissa.setCounterCount(CounterType.LOYALTY, 4);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Phyrexian Horror");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        nissa.setCounterCount(CounterType.LOYALTY, 8);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    void minusOneDestroysEnchantment() {
        addReadyNissa(player1, 5);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Ossification());
        aura.setAttachedTo(forest.getId());
        harness.activateAbility(player1, 0, 1, null, aura.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Ossification");
        harness.assertInGraveyard(player2, "Ossification");
    }

    @Test
    void ultimateLocksForestsAndCreaturesAtResolutionAndExpires() {
        addReadyNissa(player1, 7);
        harness.addToBattlefield(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Forest());
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.TRAMPLE)).isFalse();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void ultimateGrantsTrampleEvenWithoutForests() {
        addReadyNissa(player1, 7);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @CardUsed({LuxiorGiadasGift.class})
    void ultimateGrantsTrampleToNissaWhenSheIsACreature() {
        Permanent nissa = addReadyNissa(player1, 8);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new LuxiorGiadasGift());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, 0, null, nissa.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, nissa)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nissa)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nissa, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent nissa = harness.addToBattlefieldAndReturn(player, new NissaAscendedAnimist());
        nissa.setCounterCount(CounterType.LOYALTY, loyalty);
        nissa.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return nissa;
    }
}
