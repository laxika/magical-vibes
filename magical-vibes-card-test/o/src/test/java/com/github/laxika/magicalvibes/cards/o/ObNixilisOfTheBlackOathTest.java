package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObNixilisOfTheBlackOath.class})
class ObNixilisOfTheBlackOathTest extends BaseCardTest {

    @Test
    @DisplayName("+2 drains each opponent and gains the total life lost")
    void plusTwoDrainsEachOpponentAndGainsLife() {
        addReadyObNixilis(player1, 3);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("-2 creates a flying Demon and loses 2 life")
    void minusTwoCreatesDemonAndLosesLife() {
        addReadyObNixilis(player1, 2);
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(8);
        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getCard().getPower()).isEqualTo(5);
        assertThat(demon.getCard().getToughness()).isEqualTo(5);
        assertThat(demon.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("-8 creates an emblem with the sacrifice-for-power ability")
    void minusEightCreatesSacrificeAbilityEmblem() {
        addReadyObNixilis(player1, 8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).singleElement().satisfies(emblem -> assertThat(emblem.controllerId()).isEqualTo(player1.getId()));
    }

    @Test
    @DisplayName("Emblem sacrifices a Demon and uses its power")
    void emblemSacrificesDemonAndUsesItsPower() {
        assertEmblemSacrifice(0);
    }

    @Test
    @DisplayName("Emblem uses power including counters")
    void emblemUsesModifiedPower() {
        assertEmblemSacrifice(2);
    }

    private void assertEmblemSacrifice(int counters) {
        addReadyObNixilis(player1, 8);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Ob Nixilis of the Black Oath");

        addReadyObNixilis(player1, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent demon = findPermanent(player1, "Demon");
        demon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        int power = 5 + counters;
        harness.setLibrary(player1, java.util.stream.IntStream.range(0, power + 1)
                .mapToObj(i -> new ObNixilisOfTheBlackOath()).toList());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.ensurePriority(player1);

        harness.activateEmblemAbility(player1, 0, 0, null, null);
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, demon.getId());
        }
        harness.assertNotOnBattlefield(player1, "Demon");
        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertLife(player1, 10 + power);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(power);
    }

    private void addReadyObNixilis(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ObNixilisOfTheBlackOath());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
