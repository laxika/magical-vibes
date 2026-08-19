package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
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
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mind Stone");
    }

    @Test
    @DisplayName("-1 cannot target a creature")
    void minusOneCannotTargetCreature() {
        addReadyNissa(player1, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-7 boosts your creatures by the number of Forests and grants trample")
    void minusSevenBoostsOwnCreaturesByForests() {
        addReadyNissa(player1, 7);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent nissa = new Permanent(new NissaAscendedAnimist());
        nissa.setCounterCount(CounterType.LOYALTY, loyalty);
        nissa.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(nissa);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return nissa;
    }
}
