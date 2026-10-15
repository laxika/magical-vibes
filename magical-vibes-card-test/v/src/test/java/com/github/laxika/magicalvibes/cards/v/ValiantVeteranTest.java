package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValiantVeteran.class, YotianSoldier.class, GrizzlyBears.class})
class ValiantVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Veterans boost each other but never themselves")
    void veteransBoostEachOtherButNotThemselves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ValiantVeteran());
        int basePower = 2;
        int baseToughness = 2;
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ValiantVeteran());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(baseToughness);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ValiantVeteran());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(baseToughness + 1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(baseToughness + 1);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Graveyard ability includes Soldiers that enter before resolution")
    void countersSoldiersPresentAtResolution() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new ValiantVeteran());
        harness.setGraveyard(player1, List.of(new ValiantVeteran()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new ValiantVeteran());
        harness.passBothPriorities();

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graveyard ability can resolve with no Soldiers")
    void graveyardAbilityNeedsNoSoldiers() {
        Card veteran = new ValiantVeteran();
        harness.setGraveyard(player1, List.of(veteran));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Valiant Veteran");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(veteran);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Insufficient white mana cannot pay the graveyard ability cost")
    void insufficientWhiteManaDoesNotExileVeteran() {
        Card veteran = new ValiantVeteran();
        harness.setGraveyard(player1, List.of(veteran));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Valiant Veteran");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(veteran);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Other Soldiers you control get +1/+1")
    void boostsOtherSoldiersYouControl() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        Permanent nonSoldier = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentSoldier = harness.addToBattlefieldAndReturn(player2, new YotianSoldier());

        int soldierPower = gqs.getEffectivePower(gd, soldier);
        int soldierToughness = gqs.getEffectiveToughness(gd, soldier);
        harness.addToBattlefield(player1, new ValiantVeteran());

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(soldierPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(soldierToughness + 1);
        assertThat(gqs.getEffectivePower(gd, nonSoldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonSoldier)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSoldier)).isEqualTo(soldierPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentSoldier)).isEqualTo(soldierToughness);
    }

    @Test
    @DisplayName("Graveyard ability exiles Valiant Veteran and puts counters on own Soldiers")
    void graveyardAbilityExilesAndCountersOwnSoldiers() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        Permanent nonSoldier = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentSoldier = harness.addToBattlefieldAndReturn(player2, new YotianSoldier());
        Card veteran = new ValiantVeteran();
        harness.setGraveyard(player1, List.of(veteran));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Valiant Veteran");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(veteran);

        harness.passBothPriorities();

        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonSoldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentSoldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
