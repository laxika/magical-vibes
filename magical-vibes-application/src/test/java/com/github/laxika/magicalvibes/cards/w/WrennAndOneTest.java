package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrennAndOne.class, GrizzlyBears.class})
class WrennAndOneTest extends BaseCardTest {

    @Test
    @DisplayName("Enters as a land planeswalker with one loyalty")
    void entersAsLandPlaneswalker() {
        WrennAndOne card = new WrennAndOne();
        harness.addToBattlefield(player1, card);

        Permanent wrenn = findPermanent(player1, "Wrenn and One");
        assertThat(wrenn.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(wrenn.getCard().hasType(CardType.PLANESWALKER)).isTrue();
        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Nested
    @DisplayName("Loyalty abilities")
    class LoyaltyAbilities {

        @Test
        void plusOneGrantsTemporaryManaAbility() {
            Permanent wrenn = addReadyWrenn(player1, 1);

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
            assertThat(gs.getEffectiveActivatedAbilities(gd, wrenn))
                    .anyMatch(ability -> ability.getDescription().equals("{T}: Add {G}."));
        }

        @Test
        void minusOneCreatesSquirrel() {
            Permanent wrenn = addReadyWrenn(player1, 1);

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();

            assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(permanent -> permanent.getCard().getName().equals("Squirrel")
                            && permanent.getCard().getPower() == 1
                            && permanent.getCard().getToughness() == 1);
        }

        @Test
        void minusFourEmblemAddsManaForControlledCreatures() {
            addReadyWrenn(player1, 4);
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            assertThat(gd.emblems).hasSize(1);
            Emblem emblem = gd.emblems.getFirst();
            assertThat(emblem.controllerId()).isEqualTo(player1.getId());

            harness.forceStep(TurnStep.DRAW);
            harness.clearPriorityPassed();
            gs.advanceStep(gd);
            harness.passBothPriorities();

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        }
    }

    private Permanent addReadyWrenn(Player player, int loyalty) {
        Permanent perm = new Permanent(new WrennAndOne());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
